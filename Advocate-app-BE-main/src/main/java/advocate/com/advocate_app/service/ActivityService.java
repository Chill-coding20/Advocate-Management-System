package advocate.com.advocate_app.service;

import advocate.com.advocate_app.entity.Advocate;
import advocate.com.advocate_app.entity.Activity;
import advocate.com.advocate_app.repository.ActivityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ActivityService {

    @Autowired
    private ActivityRepository activityRepository;

    public void logActivity(String description, String actionType, Advocate advocate) {
        Activity activity = new Activity(description, actionType, advocate);
        activityRepository.save(activity);
    }

    public Page<Activity> getActivitiesPaged(Advocate advocate, Pageable pageable) {
        return activityRepository.findByAdvocate(advocate, pageable);
    }

    public List<Activity> getRecentActivities(Advocate advocate) {
        return activityRepository.findByAdvocateOrderByTimestampDesc(advocate);
    }
}
