package com.manilogix.modulith.auth;
import com.manilogix.modulith.auth.internal.UserAccount;
import com.manilogix.modulith.auth.internal.UserAccountRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
@Service
public class AuthDirectory {
 private final UserAccountRepository accounts;
 public AuthDirectory(UserAccountRepository accounts) {this.accounts=accounts;}
 public void requireSupplier(long id) {
  if(!accounts.existsByIdAndRole(id,UserAccount.Role.SUPPLIER))
   throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,"Supplier user does not exist");
 }
}
