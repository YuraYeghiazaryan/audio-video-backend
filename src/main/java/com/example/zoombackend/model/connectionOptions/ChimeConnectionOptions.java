package com.example.zoombackend.model.connectionOptions;

import com.amazonaws.services.chimesdkmeetings.model.CreateAttendeeResult;
import com.amazonaws.services.chimesdkmeetings.model.CreateMeetingResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@AllArgsConstructor
public class ChimeConnectionOptions extends ConnectionOptions {
    private CreateMeetingResult createMeetingResult;
    private CreateAttendeeResult createAttendeeResult;
}