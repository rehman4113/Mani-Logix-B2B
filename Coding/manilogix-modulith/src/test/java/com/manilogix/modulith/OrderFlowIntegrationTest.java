package com.manilogix.modulith;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.junit.jupiter.api.BeforeEach;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT)
class OrderFlowIntegrationTest {
 @Autowired TestRestTemplate http;
 @Autowired ObjectMapper mapper;
 @BeforeEach void supportPatch() { http.getRestTemplate().setRequestFactory(new JdkClientHttpRequestFactory()); }

 private ResponseEntity<JsonNode> call(HttpMethod method,String path,String token,Object body) throws Exception {
  HttpHeaders headers=new HttpHeaders();headers.setContentType(MediaType.APPLICATION_JSON);
  if(token!=null) headers.setBearerAuth(token);
  ResponseEntity<String> response=http.exchange(path,method,new HttpEntity<>(body,headers),String.class);
  JsonNode json=response.getBody()==null?mapper.getNodeFactory().nullNode():mapper.readTree(response.getBody());
  return new ResponseEntity<>(json,response.getStatusCode());
 }
 private String login(String email,String password) throws Exception {
  ResponseEntity<JsonNode> response=call(HttpMethod.POST,"/api/auth/login",null,Map.of("email",email,"password",password));
  assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
  return response.getBody().get("accessToken").asText();
 }
 @Test void customerOrderReservesStockAndCancellationRestoresIt() throws Exception {
  String admin=login("admin@example.test","temporary-admin-password");
  ResponseEntity<JsonNode> supplierUser=call(HttpMethod.POST,"/api/auth/users",admin,
   Map.of("email","supplier@example.test","password","temporary-supplier-password","role","SUPPLIER"));
  assertThat(supplierUser.getStatusCode()).isEqualTo(HttpStatus.CREATED);
  long userId=supplierUser.getBody().get("id").asLong();

  ResponseEntity<JsonNode> supplier=call(HttpMethod.POST,"/api/suppliers",admin,
   Map.of("name","Test supplier","email","supplier@example.test","ownerUserId",userId));
  assertThat(supplier.getStatusCode()).isEqualTo(HttpStatus.CREATED);
  long supplierId=supplier.getBody().get("id").asLong();

  ResponseEntity<JsonNode> item=call(HttpMethod.POST,"/api/inventory",admin,
   Map.of("supplierId",supplierId,"name","Test stock","quantity",5,"reorderLevel",1,"unitPrice","19.50"));
  assertThat(item.getStatusCode()).isEqualTo(HttpStatus.CREATED);
  long itemId=item.getBody().get("id").asLong();

  assertThat(call(HttpMethod.POST,"/api/auth/signup",null,
   Map.of("email","customer@example.test","password","temporary-customer-password","role","ADMIN")).getStatusCode()).isEqualTo(HttpStatus.CREATED);
  String customer=login("customer@example.test","temporary-customer-password");
  ResponseEntity<JsonNode> order=call(HttpMethod.POST,"/api/orders",customer,
   Map.of("items",List.of(Map.of("itemId",itemId,"quantity",2)),"deliveryAddress","Warehouse Road"));
  assertThat(order.getStatusCode()).isEqualTo(HttpStatus.CREATED);
  assertThat(order.getBody().get("total").decimalValue()).isEqualByComparingTo("39.00");
  long orderId=order.getBody().get("id").asLong();
  assertThat(call(HttpMethod.GET,"/api/inventory/"+itemId,customer,null).getBody().get("quantity").asInt()).isEqualTo(3);

  assertThat(call(HttpMethod.PATCH,"/api/orders/"+orderId+"/status?status=SHIPPED",customer,null).getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  assertThat(call(HttpMethod.PATCH,"/api/orders/"+orderId+"/status?status=CANCELLED",customer,null).getStatusCode()).isEqualTo(HttpStatus.OK);
  assertThat(call(HttpMethod.GET,"/api/inventory/"+itemId,customer,null).getBody().get("quantity").asInt()).isEqualTo(5);
  assertThat(call(HttpMethod.PATCH,"/api/orders/"+orderId+"/status?status=CANCELLED",customer,null).getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
 }
}
