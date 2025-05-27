package org.chumak.recommender.domain.cbr_requests;

import lombok.RequiredArgsConstructor;
import org.chumak.recommender.database.domain.cbr_requests.CbrRequest;
import org.chumak.recommender.database.domain.cbr_requests.CbrRequestsRepository;
import org.chumak.recommender.database.domain.user.User;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CbrRequestsService {

    private final CbrRequestsRepository cbrRequestsRepository;

    public void createForUser(User user) {
        Optional<CbrRequest> existingUncompletedRequest = cbrRequestsRepository.findUncompletedRequestForUser(user.getId());

        if (existingUncompletedRequest.isPresent()) {
            System.out.println("Cbr present. return;");
            return;
        }

        CbrRequest cbrRequest = new CbrRequest();
        cbrRequest.setUser(user);

        cbrRequestsRepository.save(cbrRequest);
        System.out.println("CbrRequestsService: content based recs request created for user " + user.getId());
    }

    public void completeRequest(CbrRequest cbrRequest) {
        cbrRequest.setCompletedAt(new Date());
        save(cbrRequest);
        System.out.println("CbrRequestsService: request " + cbrRequest.getId() + " completed.");
    }

    public void save(CbrRequest cbrRequest) {
        cbrRequestsRepository.save(cbrRequest);
    }

    public Optional<CbrRequest> getRequestToExecute() {
        return cbrRequestsRepository.findFirstByCompletedAtIsNull();
    }

}
