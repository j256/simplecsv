package com.j256.simplecsv.example;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import com.j256.simplecsv.common.CsvField;
import com.j256.simplecsv.processor.CsvProcessor;

/**
 * Basic example showing how we can write and read CSV files.
 * 
 * @author graywatson
 */
public class BasicExample {

	private static final String CSV_FILE_PATH = "target/" + BasicExample.class.getSimpleName() + ".csv";

	public static void main(String[] args) throws Exception {

		// create some fake accounts
		List<Account> accounts = createFakeAccounts();
		printAccounts(accounts);
		System.out.println();

		// write our CSV file
		CsvProcessor<Account> csvProcessor = new CsvProcessor<Account>(Account.class);
		File csvFile = new File(CSV_FILE_PATH);
		csvProcessor.writeAll(csvFile, accounts, true /* write header */);

		// show what's in the CSV file
		printFile(csvFile);
		System.out.println();

		// now read in the accounts
		List<Account> readAccounts =
				csvProcessor.readAll(csvFile, true /* read in header */, true /* validate header */, null);
		assertEquals(accounts, readAccounts);

		// print out our read in accounts
		printAccounts(readAccounts);
	}

	private static void printAccounts(List<Account> readAccounts) {
		for (Account account : readAccounts) {
			System.out.println(account.name + "," + account.number + "," + account.amount);
		}
	}

	private static void printFile(File csvFile) throws FileNotFoundException, IOException {
		BufferedReader reader = new BufferedReader(new FileReader(csvFile));
		try {
			while (true) {
				String line = reader.readLine();
				if (line == null) {
					break;
				}
				System.out.println(line);
			}
		} finally {
			reader.close();
		}
	}

	private static List<Account> createFakeAccounts() {
		Account account1 = new Account("Bill Smith", 1, 123123.34);
		Account account2 = new Account("Foo Bar", 2, 0.12);
		Account account3 = new Account("Jim Jimston", 3, -5125640.00);
		List<Account> accounts = Arrays.asList(account1, account2, account3);
		return accounts;
	}

	/**
	 * NOTE: this needs to be public to expose the no-arg constructor
	 */
	public static class Account {
		@CsvField(columnName = "Name")
		private String name;
		@CsvField(columnName = "Account Number")
		private long number;
		// we use a format to show currency as $1,027.99 and ($23.15)
		@CsvField(columnName = "Amount", format = "$###,##0.00;($###,##0.00)")
		private double amount;

		public Account() {
			// for simple-csv
		}

		public Account(String name, long number, double amount) {
			this.name = name;
			this.number = number;
			this.amount = amount;
		}

		public String getName() {
			return name;
		}

		public long getNumber() {
			return number;
		}

		public double getAmount() {
			return amount;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			long temp = Double.doubleToLongBits(amount);
			int result = prime + (int) (temp ^ (temp >>> 32));
			result = prime * result + ((name == null) ? 0 : name.hashCode());
			result = prime * result + (int) (number ^ (number >>> 32));
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null || getClass() != obj.getClass()) {
				return false;
			}
			Account other = (Account) obj;
			if (Double.doubleToLongBits(amount) != Double.doubleToLongBits(other.amount)) {
				return false;
			}
			if (name == null) {
				if (other.name != null) {
					return false;
				}
			} else if (!name.equals(other.name)) {
				return false;
			}
			return (number == other.number);
		}
	}
}
