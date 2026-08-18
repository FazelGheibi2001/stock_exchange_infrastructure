package com.irtech.brokerinfrastructure.readnumber;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/image")
public class ImageNumberController {

    @PostMapping("/read-number")
    public long readNumber(
            @RequestBody ImageNumberDTO dto
    ) {

        return ImageNumberReader.readNumber(
                dto.getImageAddress(),
                dto.getDigitCount()
        );
    }
}
