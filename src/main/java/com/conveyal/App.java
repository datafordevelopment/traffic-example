package com.conveyal;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import com.conveyal.osmlib.OSM;
import com.conveyal.trafficengine.GPSPoint;
import com.conveyal.trafficengine.SpeedSample;
import com.conveyal.trafficengine.SpeedSampleListener;
import com.conveyal.trafficengine.TrafficEngine;

public class App 
{
    public static void main( String[] args ) throws IOException, ParseException
    {
		OSM osm = new OSM(null);
		osm.loadFromPBFFile("./data/cebu.osm.pbf");
		
		TrafficEngine te = new TrafficEngine();
		te.setStreets(osm);
		
		List<GPSPoint> gpsPoints = loadGPSPointsFromCSV("./data/cebu-1m-sorted.csv");

		
		te.speedSampleListener = new SpeedSampleListener(){
			int n=0;
			long lastTime = System.currentTimeMillis();
			@Override
			public void onSpeedSample(SpeedSample ss) {
				n += 1;
				
				if(n%1000==0){
					long time = System.currentTimeMillis();
					double dt = (time-lastTime)/1000.0;
					double rate = 1000/dt;
					System.out.println( "rate:"+rate+" records/second");
					System.out.println( n );
					//System.out.println( ss );
					
					lastTime=time;
				}
			}
			
		};
		
		int j=0;
		for (GPSPoint gpsPoint : gpsPoints) {
			j++;
			if(j%1000==0){
				System.out.println(String.format("%d/%d gps point read", j, gpsPoints.size()));
			}
			te.update(gpsPoint);
		}
    }
    
	private static List<GPSPoint> loadGPSPointsFromCSV(String string) throws IOException, ParseException {
		List<GPSPoint> ret = new ArrayList<GPSPoint>();

		File csvData = new File(string);
		CSVParser parser = CSVParser.parse(csvData, Charset.forName("UTF-8"), CSVFormat.RFC4180);

		DateFormat formatter = new TaxiCsvDateFormatter();

		int i = 0;
		for (CSVRecord csvRecord : parser) {
			if (i % 10000 == 0) {
				System.out.println(i);
			}

			String timeStr = csvRecord.get(0);
			String vehicleId = csvRecord.get(1);
			String lonStr = csvRecord.get(2);
			String latStr = csvRecord.get(3);

			Date dt = formatter.parse(timeStr);
			long time = dt.getTime();

			GPSPoint pt = new GPSPoint(time, vehicleId, Double.parseDouble(lonStr), Double.parseDouble(latStr));
			ret.add(pt);

			i++;
		}

		return ret;
	}
}
