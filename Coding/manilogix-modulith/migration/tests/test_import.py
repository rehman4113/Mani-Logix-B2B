import copy
import hashlib
import json
import os
import sys
import unittest
from pathlib import Path
from urllib.parse import urlsplit, urlunsplit
sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
from export_legacy import mysql_connection
from import_to_mysql import apply, prepare

SAMPLE = {
 "auth_users":[
  {"id":1,"email":"admin@example.test","password":"$2a$12$"+"a"*53},
  {"id":2,"email":"supplier@example.test","password":"$2a$12$"+"b"*53}],
 "auth_roles":[{"id":1,"name":"ADMIN"},{"id":2,"name":"SUPPLIER"}],
 "auth_user_roles":[{"userId":1,"roleId":1},{"userId":2,"roleId":2}],
 "suppliers":[{"id":7,"name":"Warehouse","email":"supplier@example.test","created_at":"2025-01-01T10:00:00"}],
 "inventory":[{"_id":"507f1f77bcf86cd799439011","name":"Widget","supplierId":"7","category":"Parts","quantity":10,"reorderLevel":2}],
 "orders":[{"order_id":15,"customer_id":"old-customer","status":"DISPATCHED"}],
 "order_items":[{"order_order_id":15,"item_ids":"507f1f77bcf86cd799439011"}],
 "notifications":[{"_id":"507f1f77bcf86cd799439012","userId":"admin","message":"Existing alert","type":"ALERT"}]
}
class ImportTest(unittest.TestCase):
 def test_preflight_rejects_missing_owner(self):
  snapshot=copy.deepcopy(SAMPLE);snapshot["suppliers"][0]["email"]="unknown@example.test"
  with self.assertRaisesRegex(ValueError,"SUPPLIER account mapping"): prepare(snapshot,{})
 def test_preflight_rejects_multiple_roles(self):
  snapshot=copy.deepcopy(SAMPLE);snapshot["auth_user_roles"].append({"userId":2,"roleId":1})
  with self.assertRaisesRegex(ValueError,"exactly one supported role"): prepare(snapshot,{})
 @unittest.skipUnless(os.getenv("TEST_MYSQL_URL"),"MySQL URL not configured")
 def test_mysql_import_is_complete_and_repeatable(self):
  admin=mysql_connection(os.environ["DB_TEST_ADMIN_URL"])
  try:
   with admin.cursor() as cursor: cursor.execute("CREATE DATABASE manilogix_import")
  finally: admin.close()
  connection=mysql_connection(os.environ["TEST_MYSQL_URL"])
  try:
   with connection.cursor() as cursor:
    for path in sorted((Path(__file__).resolve().parents[2]/"src/main/resources/db/migration").glob("V*.sql")):
     for statement in path.read_text().split(";"):
      if statement.strip(): cursor.execute(statement)
   encoded=json.dumps(SAMPLE,sort_keys=True).encode()
   checksum=hashlib.sha256(encoded).hexdigest()
   prepared=prepare(SAMPLE,{})
   apply(connection,SAMPLE,prepared,checksum)
   apply(connection,SAMPLE,prepared,checksum)
   with connection.cursor() as cursor:
    for table,expected in (("app_users",2),("suppliers",1),("inventory_items",1),
                            ("legacy_records",2),("legacy_migration_runs",1),("purchase_orders",0)):
     cursor.execute("SELECT COUNT(*) AS count FROM "+table)
     self.assertEqual(cursor.fetchone()["count"],expected)
    cursor.execute("SELECT active,needs_price_review,quantity,category,legacy_mongo_id FROM inventory_items")
    item=cursor.fetchone()
    self.assertEqual((item["active"],item["needs_price_review"],item["quantity"],item["category"],item["legacy_mongo_id"]),
     (0,1,10,"Parts","507f1f77bcf86cd799439011"))
    cursor.execute("SELECT payload_json FROM legacy_records WHERE source_type='ORDER'")
    archived=json.loads(cursor.fetchone()["payload_json"])
    self.assertEqual(archived["itemRows"][0]["item_ids"],"507f1f77bcf86cd799439011")
  finally: connection.close()
if __name__=="__main__": unittest.main()
