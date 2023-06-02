package com.study.springdata.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author osmondy
 */
@Component
public class DateConverter implements Converter<String, Date> {

    @Override
    public Date convert(@NonNull String source) {
        try {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(source);
        } catch (ParseException e) {
            try {
                return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'+08:00'").parse(source);
            } catch (ParseException ex) {
                try {
                    return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(source);
                } catch (ParseException ex2) {
                    ex2.printStackTrace();
                }
            }
        }
        return null;
    }
}

