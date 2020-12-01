package com.project.passbook.couponService.entities;

import com.google.common.base.Enums;
import com.project.passbook.couponService.constants.FeedbackType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Feedback {
    // use as row key
    private String feedbackId;

    private String customerId;
    private String type;
    private String templateId;
    private String comment;

    public boolean validate() {
        FeedbackType feedbackType = Enums.getIfPresent(FeedbackType.class, this.type.toUpperCase()).orNull();

        return !(null == feedbackType || null == comment);
    }
}