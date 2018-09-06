package com.dwarfcu.udemy.schemaregistryandrestproxy.avro.generic;

import org.apache.avro.Schema;
import org.apache.avro.file.DataFileReader;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.generic.*;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DatumWriter;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;

public class DwarfCuRecordExamples {
  public static void main(String[] args) {

    // Create schema
    Schema.Parser parser = new Schema.Parser();

    Schema schemaTrayectoria = parser.parse("{\n" +
        " \"type\": \"record\",\n" +
        " \"namespace\": \"com.dwarfcu\",\n" +
        " \"name\": \"trayectoria\",\n" +
        " \"doc\": \"Trayectoria de un ciclista\",\n" +
        " \"fields\":  [\n" +
        "   { \"name\": \"equipo\", \"type\": \"string\", \"doc\": \"...\" },\n" +
        "   { \"name\": \"fecha_inicio\", \"type\": \"int\", \"logicalType\": \"date\" },\n" +
        "   { \"name\": \"fecha_fin\", \"type\": \"int\", \"logicalType\": \"date\" }\n" +
        " ]\n" +
        "}"
    );
    Schema schemaCiclista = parser.parse("{\n" +
        " \"type\": \"record\",\n" +
        " \"namespace\": \"com.dwarfcu\",\n" +
        " \"name\": \"ciclistas\",\n" +
        " \"doc\": \"Avro schema para ciclistas\",\n" +
        " \"fields\": [\n" +
        "   { \"name\": \"nombre\", \"type\": \"string\", \"doc\": \"...\" },\n" +
        "   { \"name\": \"altura\", \"type\": \"int\", \"doc\": \"en cms\" },\n" +
        "   { \"name\": \"peso\", \"type\": \"float\", \"doc\": \"en Kgs\" },\n" +
        "   { \"name\": \"activo\", \"type\": \"boolean\", \"doc\": \"default true\" },\n" +
        "   { \"name\": \"emails\", \"type\": { \"type\": \"array\", \"items\": \"string\"}, \"default\": [] },\n" +
        "   { \"name\": \"trayectoria\", \"type\": { \"type\": \"array\", \"items\": \"com.dwarfcu.trayectoria\" }, \"doc\": \"\" },\n" +
        "   { \"name\": \"strava\", \"type\": [\"null\", \"string\"], \"default\": null, \"doc\": \"URL de su cuenta de Strava\" }\n" +
        " ]\n" +
        "}"
    );

    LocalDate epoch = LocalDate.ofEpochDay(0);

    // Create a generic record
    GenericRecordBuilder reynolds = new GenericRecordBuilder(schemaTrayectoria);
    reynolds.set("equipo", "reynolds");
    reynolds.set("fecha_inicio", ChronoUnit.DAYS.between(epoch, LocalDate.of(1984, 1,1)));
    reynolds.set("fecha_fin", ChronoUnit.DAYS.between(epoch, LocalDate.of(1986, 12,31)));

    GenericRecordBuilder reynolds_seur = new GenericRecordBuilder(schemaTrayectoria);
    reynolds_seur.set("equipo", "reynolds_seur");
    reynolds_seur.set("fecha_inicio", ChronoUnit.DAYS.between(epoch, LocalDate.of(1987, 1,1)));
    reynolds_seur.set("fecha_fin", ChronoUnit.DAYS.between(epoch, LocalDate.of(1987, 12,31)));

    GenericRecordBuilder ciclistaBuilder = new GenericRecordBuilder(schemaCiclista);
    ciclistaBuilder.set("nombre", "Miguel Indurain");
    ciclistaBuilder.set("altura", 188);
    ciclistaBuilder.set("peso", 80.0f);
    ciclistaBuilder.set("activo", false);
    ciclistaBuilder.set("trayectoria", Arrays.asList(reynolds.build(), reynolds_seur.build()));
    GenericData.Record ciclista = ciclistaBuilder.build();
    System.out.println(ciclista);

    // Write that generic record to a file
    final DatumWriter<GenericRecord> datumWriter = new GenericDatumWriter<>(schemaCiclista);
    try (DataFileWriter<GenericRecord> dataFileWriter = new DataFileWriter<>(datumWriter)) {
      dataFileWriter.create(ciclista.getSchema(), new File("ciclista-generic.avro"));
      dataFileWriter.append(ciclista);
      System.out.println("ciclista-generic.avro");
    } catch (IOException e) {
      System.out.println("Couldn't write file");
      e.printStackTrace();
    }

    // Read a generic record from a file
    final File file = new File("ciclista-generic.avro");
    final DatumReader<GenericRecord> datumReader = new GenericDatumReader<>();
    GenericRecord ciclistaRead;
    try (DataFileReader<GenericRecord> dataFileReader = new DataFileReader<>(file, datumReader)){
      ciclistaRead = dataFileReader.next();
      System.out.println("Successfully read avro file");
      System.out.println(ciclistaRead.toString());

      // get the data from the generic record
      System.out.println("Nombre: " + ciclistaRead.get("nombre"));

      // read a non existent field
      System.out.println("Campo inexistente: " + ciclistaRead.get("no_existe"));

      // get the full record
      System.out.println(ciclistaRead.toString());
    }
    catch(IOException e) {
      e.printStackTrace();
    }
  }
}