package com.github.smuddgge.test.database;

import com.github.smuddgge.squishydatabase.record.Field;
import com.github.smuddgge.squishydatabase.record.Record;
import com.github.smuddgge.squishydatabase.record.RecordFieldType;

public class PlayerRecord extends Record {

    @Field(type = RecordFieldType.PRIMARY)
    public String uuid;

    public String name;
}
