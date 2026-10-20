#!/usr/bin/env python3
"""Read-only snapshot of the three legacy MySQL services and two Mongo collections."""
import argparse
import datetime
import decimal
import json
import os
from pathlib import Path
from urllib.parse import urlsplit, unquote
import pymysql
from pymongo import MongoClient
from bson import ObjectId

def mysql_connection(uri):
    parsed = urlsplit(uri)
    if parsed.scheme != "mysql" or not parsed.hostname or not parsed.path:
        raise ValueError("Expected mysql://user:password@host:3306/database")
    return pymysql.connect(host=parsed.hostname, port=parsed.port or 3306,
        user=unquote(parsed.username or ""), password=unquote(parsed.password or ""),
        database=unquote(parsed.path.lstrip("/")), charset="utf8mb4",
        cursorclass=pymysql.cursors.DictCursor, autocommit=True)

def rows(connection, candidates, required=True):
    with connection.cursor() as cursor:
        cursor.execute("SHOW TABLES")
        names = {str(next(iter(row.values()))).lower(): next(iter(row.values())) for row in cursor.fetchall()}
        table = next((names[name] for name in candidates if name in names), None)
        if table is None:
            if required: raise ValueError("Missing legacy table: " + " or ".join(candidates))
            return []
        cursor.execute("SELECT * FROM `" + table.replace("`", "``") + "`")
        return list(cursor.fetchall())

def field(row, candidates):
    names = {key.lower(): key for key in row}
    for candidate in candidates:
        if candidate in names: return row[names[candidate]]
    raise ValueError("Cannot determine legacy join column among: " + ", ".join(row))

def mongo_rows(uri, db_name, collection):
    client = MongoClient(uri, serverSelectionTimeoutMS=10000)
    try:
        return list(client[db_name][collection].find({}))
    finally:
        client.close()

def default(value):
    if isinstance(value, (datetime.datetime, datetime.date)): return value.isoformat()
    if isinstance(value, decimal.Decimal): return str(value)
    if isinstance(value, ObjectId): return str(value)
    raise TypeError("Unsupported source field type: " + type(value).__name__)

def export():
    parser = argparse.ArgumentParser()
    parser.add_argument("--output", required=True)
    args = parser.parse_args()
    output = Path(args.output)
    if output.exists(): raise SystemExit("Refusing to overwrite an existing snapshot")
    auth = mysql_connection(os.environ["LEGACY_AUTH_MYSQL_URL"])
    supplier = mysql_connection(os.environ["LEGACY_SUPPLIER_MYSQL_URL"])
    order = mysql_connection(os.environ["LEGACY_ORDER_MYSQL_URL"])
    try:
        users = rows(auth, ["user", "users"])
        roles = rows(auth, ["role", "roles"])
        joins = rows(auth, ["user_roles", "users_roles", "user_role"])
        normalized_joins = [{"userId": field(j, ["user_id", "users_id", "user_id_id"]),
                             "roleId": field(j, ["roles_id", "role_id", "roles_id_id"])} for j in joins]
        orders = rows(order, ["orders"])
        order_items = rows(order, ["order_item_ids", "orders_item_ids"], required=bool(orders))
        snapshot = {
            "auth_users": users, "auth_roles": roles, "auth_user_roles": normalized_joins,
            "suppliers": rows(supplier, ["supplier", "suppliers"]),
            "orders": orders, "order_items": order_items,
            "inventory": mongo_rows(os.environ["LEGACY_INVENTORY_MONGO_URI"], os.environ["LEGACY_INVENTORY_MONGO_DB"], "inventory"),
            "notifications": mongo_rows(os.environ["LEGACY_NOTIFICATION_MONGO_URI"], os.environ["LEGACY_NOTIFICATION_MONGO_DB"], "notifications"),
        }
        output.parent.mkdir(parents=True, exist_ok=True)
        fd = os.open(output, os.O_WRONLY | os.O_CREAT | os.O_EXCL, 0o600)
        with os.fdopen(fd, "w", encoding="utf-8") as handle:
            json.dump(snapshot, handle, default=default, ensure_ascii=False, sort_keys=True, separators=(",", ":"))
        print("Snapshot saved with restrictive permissions. Counts:", {k: len(v) for k,v in snapshot.items()})
    finally:
        auth.close(); supplier.close(); order.close()

if __name__ == "__main__":
    export()
