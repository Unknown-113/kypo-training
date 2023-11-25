package cz.muni.ics.kypo.training.converters;

import com.fasterxml.jackson.databind.util.StdConverter;

import java.time.Duration;

public class DurationConverter extends StdConverter<Duration, String> {

    @Override
    public String convert(Duration duration) {
        long seconds = duration.getSeconds();
        long HH = seconds / 3600;
        long MM = (seconds % 3600) / 60;
        long SS = seconds % 60;
        return String.format("%02d:%02d:%02d", HH, MM, SS);
    }
}


