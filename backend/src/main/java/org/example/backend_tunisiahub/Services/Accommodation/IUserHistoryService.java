package org.example.backend_tunisiahub.Services.Accommodation;

import org.example.backend_tunisiahub.Entities.Accommodation.UserHistory;
import org.example.backend_tunisiahub.Entities.User.User;

import java.util.List;

public interface IUserHistoryService {
    public void trackView(Long accommodation, String email);
    public List<UserHistory> getUserHistory(String email);
}
