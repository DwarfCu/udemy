package com.dwarfcu.udemy.schemaregistryandrestproxy.avro.specific;

import com.dwarfcu.ciclistas;
import com.dwarfcu.trayectoria;
import org.apache.avro.file.DataFileReader;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificDatumWriter;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;

public class DwarfCuSpecificRecordExamples {
  public static void main(String[] args) {

    LocalDate epoch = LocalDate.ofEpochDay(0);

    // Create a specific record
    // Option 1)
    trayectoria trayectoria1 = trayectoria.newBuilder()
        .setEquipo("Reynolds")
        .setFechaInicio((int) ChronoUnit.DAYS.between(epoch, LocalDate.of(1982, 1,1)))
        .setFechaFin((int) ChronoUnit.DAYS.between(epoch, LocalDate.of(1984, 12,31)))
        .build();

    // Option 2)
    trayectoria.Builder trayectoria2Builder = trayectoria.newBuilder();
    trayectoria2Builder.setEquipo("Seat-Orbea");
    trayectoria2Builder.setFechaInicio((int) ChronoUnit.DAYS.between(epoch, LocalDate.of(1985, 1,1)));
    trayectoria2Builder.setFechaFin((int) ChronoUnit.DAYS.between(epoch, LocalDate.of(1985, 12,31)));
    trayectoria trayectoria2 = trayectoria2Builder.build();

    ciclistas.Builder ciclistaBuilder = ciclistas.newBuilder();
    ciclistaBuilder.setNombre("Pedro Delgado");
    ciclistaBuilder.setAltura(171);
    ciclistaBuilder.setPeso(64f);
    ciclistaBuilder.setActivo(false);
    ciclistaBuilder.setTrayectoria(Arrays.asList(trayectoria1, trayectoria2));

    ciclistas ciclista = ciclistaBuilder.build();

    System.out.println(ciclista);

    // Write to file
    final DatumWriter<ciclistas> datumWriter = new SpecificDatumWriter<>(ciclistas.class);

    try (DataFileWriter<ciclistas> dataFileWriter = new DataFileWriter<>(datumWriter)) {
      dataFileWriter.create(ciclista.getSchema(), new File("ciclista-specific.avro"));
      dataFileWriter.append(ciclista);
      System.out.println("successfully wrote ciclista-specific.avro");
    } catch (IOException e){
      e.printStackTrace();
    }

    // Read from file
    final File file = new File("ciclista-specific.avro");
    final DatumReader<ciclistas> datumReader = new SpecificDatumReader<>(ciclistas.class);
    final DataFileReader<ciclistas> dataFileReader;
    try {
      System.out.println("Reading our specific record");
      dataFileReader = new DataFileReader<>(file, datumReader);
      while (dataFileReader.hasNext()) {
        ciclistas readCiclista = dataFileReader.next();
        System.out.println(readCiclista.toString());
        System.out.println("Nombre: " + readCiclista.getNombre());
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

  }
}