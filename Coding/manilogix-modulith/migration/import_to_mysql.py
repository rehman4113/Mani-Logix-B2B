#!/usr/bin/env python3
"""Validate offline, then transactionally import a frozen legacy snapshot into an EMPTY Modulith DB."""
import argparse
import datetime as dt
import hashlib
import json
import os
from pathlib import Path
from export_legacy import mysql_connection

KEYS = ("auth_users","auth_roles","auth_user_roles","suppliers","orders","order_items","inventory","notifications")
def value(row, *names):
    lower={key.lower(): val for key,val in row.items()}
    for name in names:
        if name.lower() in lower: return lower[name.lower()]
    return None
def positive_id(raw, context):
    try:
        result=int(raw)
        if result<1: raise ValueError()
        return result
    except (ValueError,TypeError): raise ValueError(context + ": expected positive numeric ID") from None
def utc_datetime(raw):
    if not raw: return dt.datetime.now(dt.timezone.utc).replace(tzinfo=None)
    item=dt.datetime.fromisoformat(str(raw).replace("Z","+00:00"))
    if item.tzinfo is None: item=item.replace(tzinfo=dt.timezone.utc)
    return item.astimezone(dt.timezone.utc).replace(tzinfo=None)
def strict_text(raw, context):
    result=str(raw).strip() if raw is not None else ""
    if not result: raise ValueError(context + ": missing text")
    return result

def prepare(snapshot, owners):
    for key in KEYS:
        if not isinstance(snapshot.get(key), list): raise ValueError("Missing snapshot array: "+key)
    roles={positive_id(value(row,"id"),"role"):strict_text(value(row,"name"),"role name").upper()
           for row in snapshot["auth_roles"]}
    by_user={}
    for join in snapshot["auth_user_roles"]:
        by_user.setdefault(positive_id(join["userId"],"role user"),set()).add(roles[positive_id(join["roleId"],"role ID")])
    users=[];by_email={};by_id={}
    for row in snapshot["auth_users"]:
        user_id=positive_id(value(row,"id"),"user ID")
        email=strict_text(value(row,"email"),"email").lower()
        password=strict_text(value(row,"password"),"password hash")
        if not password.startswith(("$2a$","$2b$","$2y$")): raise ValueError("User "+str(user_id)+" has an unsupported password hash")
        user_roles=by_user.get(user_id,set())
        if len(user_roles)!=1 or not user_roles <= {"ADMIN","MANAGER","SUPPLIER"}:
            raise ValueError("User "+str(user_id)+" needs exactly one supported role; resolve roles before importing")
        if user_id in by_id or email in by_email: raise ValueError("Duplicate user ID or email")
        user={"id":user_id,"email":email,"password":password,"role":next(iter(user_roles)),
              "firstName":value(row,"first_name","firstName"),"lastName":value(row,"last_name","lastName")}
        users.append(user);by_email[email]=user;by_id[user_id]=user
    suppliers=[];supplier_by_id={};supplier_by_email={}
    for row in snapshot["suppliers"]:
        old_id=positive_id(value(row,"id"),"supplier ID")
        email=strict_text(value(row,"email"),"supplier email").lower()
        mapped=owners.get(str(old_id))
        owner=by_id.get(positive_id(mapped,"supplier owner mapping")) if mapped is not None else by_email.get(email)
        if owner is None or owner["role"]!="SUPPLIER": raise ValueError("Supplier "+str(old_id)+" needs a SUPPLIER account mapping")
        if old_id in supplier_by_id: raise ValueError("Duplicate supplier ID")
        name=strict_text(value(row,"name"),"supplier name")
        item={"id":old_id,"name":name,"email":email,"ownerId":owner["id"],
              "contact":value(row,"contact_number","contactNumber"),"address":value(row,"address"),
              "company":value(row,"company"),"createdAt":utc_datetime(value(row,"created_at","createdAt"))}
        suppliers.append(item);supplier_by_id[old_id]=item
        supplier_by_email.setdefault(email,[]).append(item)
    inventory=[];seen_mongo=set()
    for row in snapshot["inventory"]:
        mongo_id=strict_text(value(row,"_id","id"),"inventory Mongo ID")
        if mongo_id in seen_mongo: raise ValueError("Duplicate inventory Mongo ID")
        seen_mongo.add(mongo_id)
        source=strict_text(value(row,"supplierId","supplier_id"),"inventory supplierId")
        target=supplier_by_id.get(int(source)) if source.isdecimal() else None
        if target is None:
            candidates=supplier_by_email.get(source.lower(),[])
            if len(candidates)!=1: raise ValueError("Inventory "+mongo_id+" has unresolved or ambiguous supplierId "+source)
            target=candidates[0]
        quantity=int(value(row,"quantity") or 0)
        reorder=int(value(row,"reorderLevel","reorder_level") or 0)
        if quantity<0 or reorder<0: raise ValueError("Inventory "+mongo_id+" has negative stock settings")
        inventory.append({"mongoId":mongo_id,"supplierId":target["id"],
            "name":strict_text(value(row,"name"),"inventory name"),"category":value(row,"category"),
            "quantity":quantity,"reorder":reorder})
    ids=set()
    for source_type,rows in (("ORDER",snapshot["orders"]),("NOTIFICATION",snapshot["notifications"])):
        for row in rows:
            source_id=strict_text(value(row,"order_id","orderId","id","_id"),source_type+" ID")
            if (source_type,source_id) in ids: raise ValueError("Duplicate "+source_type+" ID")
            ids.add((source_type,source_id))
    return {"users":users,"suppliers":suppliers,"inventory":inventory,
       "orders":snapshot["orders"],"orderItems":snapshot["order_items"],
       "notifications":snapshot["notifications"]}

def apply(connection, snapshot, prepared, checksum):
    with connection.cursor() as cur:
        cur.execute("SELECT GET_LOCK('manilogix_legacy_import',10)")
        if next(iter(cur.fetchone().values())) != 1: raise ValueError("Another import is running")
    try:
        with connection.cursor() as cur:
            cur.execute("SELECT snapshot_sha256 FROM legacy_migration_runs")
            existing=[next(iter(row.values())) for row in cur.fetchall()]
            if existing:
                if existing==[checksum]:
                    print("This exact snapshot was already imported; no writes performed")
                    return
                raise ValueError("Target has another migration snapshot; use a new empty target")
            for table in ("app_users","suppliers","inventory_items","purchase_orders","notifications","legacy_records"):
                cur.execute("SELECT COUNT(*) FROM `"+table+"`")
                if next(iter(cur.fetchone().values())): raise ValueError("Target table "+table+" is not empty; no writes performed")
        connection.begin()
        try:
            with connection.cursor() as cur:
                for user in prepared["users"]:
                    cur.execute("INSERT INTO app_users(id,email,password_hash,role,first_name,last_name) VALUES (%s,%s,%s,%s,%s,%s)",
                        (user["id"],user["email"],user["password"],user["role"],user["firstName"],user["lastName"]))
                for supplier in prepared["suppliers"]:
                    cur.execute("""INSERT INTO suppliers(id,name,email,owner_user_id,active,created_at,contact_number,address,company)
                        VALUES (%s,%s,%s,%s,1,%s,%s,%s,%s)""",
                        (supplier["id"],supplier["name"],supplier["email"],supplier["ownerId"],supplier["createdAt"],
                         supplier["contact"],supplier["address"],supplier["company"]))
                for item in prepared["inventory"]:
                    # Old Mongo documents contain no price. Keep stock inactive until a user sets one.
                    cur.execute("""INSERT INTO inventory_items(supplier_id,name,category,legacy_mongo_id,quantity,reorder_level,unit_price,active,needs_price_review)
                        VALUES (%s,%s,%s,%s,%s,%s,0,0,1)""",
                        (item["supplierId"],item["name"],item["category"],item["mongoId"],item["quantity"],item["reorder"]))
                by_order={}
                for line in prepared["orderItems"]:
                    order_id=value(line,"order_order_id","orders_order_id","order_id")
                    if order_id is None: raise ValueError("Unknown legacy order-item join column")
                    by_order.setdefault(str(order_id),[]).append(line)
                for order in prepared["orders"]:
                    order_id=strict_text(value(order,"order_id","orderId","id"),"order ID")
                    payload={"order":order,"itemRows":by_order.pop(order_id,[])}
                    cur.execute("INSERT INTO legacy_records(source_type,source_id,payload_json,review_status) VALUES ('ORDER',%s,%s,'REQUIRES_REVIEW')",
                        (order_id,json.dumps(payload,sort_keys=True,default=str)))
                if by_order: raise ValueError("Order item rows exist without a parent order")
                for note in prepared["notifications"]:
                    mongo_id=strict_text(value(note,"_id","id"),"notification Mongo ID")
                    cur.execute("INSERT INTO legacy_records(source_type,source_id,payload_json,review_status) VALUES ('NOTIFICATION',%s,%s,'REQUIRES_REVIEW')",
                        (mongo_id,json.dumps(note,sort_keys=True,default=str)))
                summary={k:len(prepared[k]) for k in ("users","suppliers","inventory","orders","orderItems","notifications")}
                cur.execute("INSERT INTO legacy_migration_runs(snapshot_sha256,summary_json) VALUES (%s,%s)",
                    (checksum,json.dumps(summary,sort_keys=True)))
            connection.commit()
        except BaseException:
            connection.rollback()
            raise
        print("Committed import:",summary)
    finally:
        with connection.cursor() as cur: cur.execute("SELECT RELEASE_LOCK('manilogix_legacy_import')")

def main():
    parser=argparse.ArgumentParser()
    parser.add_argument("--snapshot",required=True)
    parser.add_argument("--owner-map",help="JSON object mapping legacy supplier IDs to legacy user IDs")
    parser.add_argument("--apply",action="store_true",help="Write only to an empty MySQL target")
    args=parser.parse_args()
    data=Path(args.snapshot).read_bytes()
    snapshot=json.loads(data)
    owners=json.loads(Path(args.owner_map).read_text()) if args.owner_map else {}
    prepared=prepare(snapshot,owners)
    summary={k:len(prepared[k]) for k in ("users","suppliers","inventory","orders","orderItems","notifications")}
    checksum=hashlib.sha256(data).hexdigest()
    print("Preflight OK:",summary,"SHA-256:",checksum)
    if not args.apply: return
    connection=mysql_connection(os.environ["TARGET_MYSQL_URL"])
    try: apply(connection,snapshot,prepared,checksum)
    finally: connection.close()
if __name__=="__main__":
    main()
