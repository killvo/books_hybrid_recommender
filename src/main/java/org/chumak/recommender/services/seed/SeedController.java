package org.chumak.recommender.services.seed;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/seed")
@RequiredArgsConstructor
public class SeedController {

    private final SeedService seedService;

    @PostMapping()
    public void seedDatabase() {
        seedService.seedDatabase();
    }
}
