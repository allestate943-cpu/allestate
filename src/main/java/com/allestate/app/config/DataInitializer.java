package com.allestate.app.config;

import com.allestate.app.model.Listing;
import com.allestate.app.repository.ListingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DataInitializer implements CommandLineRunner {

    private final ListingRepository listingRepository;

    @Autowired
    public DataInitializer(ListingRepository listingRepository) {
        this.listingRepository = listingRepository;
    }

    @Override
    public void run(String... args) {
        try {
            if (listingRepository.count() == 0) {
                Listing l1 = new Listing(
                        "3 BHK Apartment in Koramangala",
                        "Spacious 3 BHK with balcony and covered parking",
                        new BigDecimal("8500000"),
                        "Bengaluru",
                        "Koramangala",
                        12.9346,
                        77.6266,
                        3,
                        1500,
                        "SALE"
                );

                Listing l2 = new Listing(
                        "2 BHK for Rent near MG Road",
                        "Well-maintained 2 BHK, close to metro and shopping",
                        new BigDecimal("25000"),
                        "Bengaluru",
                        "MG Road",
                        12.9719,
                        77.5946,
                        2,
                        900,
                        "RENT"
                );

                listingRepository.save(l1);
                listingRepository.save(l2);
                System.out.println("[DataInitializer] Inserted sample listings");
            }
        } catch (Exception ex) {
            // Do not fail startup if DB is not available; just log
            System.out.println("[DataInitializer] Skipping sample data load: " + ex.getMessage());
        }
    }
}

