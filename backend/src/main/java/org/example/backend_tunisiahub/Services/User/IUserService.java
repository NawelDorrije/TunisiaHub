package org.example.backend_tunisiahub.Services.User;

import org.example.backend_tunisiahub.Entities.User.User;
import java.util.List;

public interface IUserService {

  List<User> retrieveAllUsers();

  User retrieveUser(Long id);

  User addUser(User user);

  void deleteUser(Long id);

  User modifyUser(User user);
}
