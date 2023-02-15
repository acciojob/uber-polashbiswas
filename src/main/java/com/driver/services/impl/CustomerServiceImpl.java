package com.driver.services.impl;

import com.driver.model.TripBooking;
import com.driver.services.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.driver.model.Customer;
import com.driver.model.Driver;
import com.driver.repository.CustomerRepository;
import com.driver.repository.DriverRepository;
import com.driver.repository.TripBookingRepository;
import com.driver.model.TripStatus;

import java.util.ArrayList;
import java.util.List;

@Service
public class CustomerServiceImpl implements CustomerService {

	@Autowired
	CustomerRepository customerRepository2;

	@Autowired
	DriverRepository driverRepository2;

	@Autowired
	TripBookingRepository tripBookingRepository2;

	@Override
	public void register(Customer customer) {
		//Save the customer in database
		customerRepository2.save(customer);
	}

	@Override
	public void deleteCustomer(Integer customerId) {
		// Delete customer without using deleteById function
		Customer customer = customerRepository2.findById(customerId).get();
		customerRepository2.delete(customer);
	}

	@Override
	public TripBooking bookTrip(int customerId, String fromLocation, String toLocation, int distanceInKm) throws Exception{
		//Book the driver with lowest driverId who is free (cab available variable is Boolean.TRUE). If no driver is available, throw "No cab available!" exception
		//Avoid using SQL query
		List<Driver> drivers = driverRepository2.findAll();
		TripBooking tripBooking = new TripBooking();
		boolean flag = false;
		for (Driver driver : drivers) {
			if (driver.getCab().getAvailable() == true) {
				driver.getCab().setAvailable(false);
				tripBooking.setStatus(TripStatus.CONFIRMED);
				tripBooking.setBill(driver.getCab().getPerKmRate() * distanceInKm);
				tripBooking.setFromLocation(fromLocation);
				tripBooking.setToLocation(toLocation);
				tripBooking.setDistanceInKm(distanceInKm);


				Customer customer = customerRepository2.findById(customerId).get();
				List<TripBooking> list = customer.getTripBookingList();
				list.add(tripBooking);
				customer.setTripBookingList(list);
				tripBooking.setCustomer(customer);

				List<TripBooking> list1 = driver.getTripBooking();
				list1.add(tripBooking);
				driver.setTripBooking(list1);
				tripBooking.setDriver(driver);

				driverRepository2.save(driver);
				customerRepository2.save(customer);
				flag = true;
				break;
			}
		}
		if (flag == false) {
			throw new Exception("No cab available!");
		} else {
			return tripBooking;
		}
//		TripBooking tripBooking = new TripBooking();
//		Driver driver = null;
//
//		List<Driver> allDrivers = driverRepository2.findAll();
//
//		for (Driver driver1: allDrivers){
//			//finding if they are available or not
//			if (driver1.getCab().getAvailable() == Boolean.TRUE){
//				if ((driver == null) || (driver.getDriverId() > driver1.getDriverId())){
//					driver = driver1;
//				}
//			}
//		}
//		if (driver == null){
//			throw new Exception("No cab available");
//		}
//
//		//set attribute of entity layer
//		Customer customer = customerRepository2.findById(customerId).get();
//
//		tripBooking.setCustomer(customer);
//		tripBooking.setDriver(driver);
//		driver.getCab().setAvailable(Boolean.FALSE);
//		tripBooking.setBill(driver.getCab().getPerKmRate() * distanceInKm);
//		tripBooking.setFromLocation(fromLocation);
//		tripBooking.setToLocation(toLocation);
//		tripBooking.setDistanceInKm(distanceInKm);
//
//		tripBooking.setStatus(TripStatus.CONFIRMED);
//
//		//Set bidirectional mapping
//		customer.getTripBookingList().add(tripBooking);
//		customerRepository2.save(customer);
//		driver.getTripBooking().add(tripBooking);
//		driverRepository2.save(driver);
//		return tripBooking;

	}

	@Override
	public void cancelTrip(Integer tripId){
		//Cancel the trip having given trip Id and update TripBooking attributes accordingly
		TripBooking tripBooking = tripBookingRepository2.findById(tripId).get();
		tripBooking.setStatus(TripStatus.CANCELED);
		tripBooking.setBill(0);
		tripBooking.getDriver().getCab().setAvailable(Boolean.TRUE);
		tripBookingRepository2.save(tripBooking);

	}

	@Override
	public void completeTrip(Integer tripId){
		//Complete the trip having given trip Id and update TripBooking attributes accordingly
		TripBooking tripBooking = tripBookingRepository2.findById(tripId).get();
		tripBooking.setStatus(TripStatus.COMPLETED);
		int bill = tripBooking.getDriver().getCab().getPerKmRate()*tripBooking.getDistanceInKm();
		tripBooking.setBill(bill);
		tripBooking.getDriver().getCab().setAvailable(Boolean.TRUE);
		tripBookingRepository2.save(tripBooking);
	}
}
