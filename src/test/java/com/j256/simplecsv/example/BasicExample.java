package com.j256.simplecsv.example;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
		new BasicExample().doMain();
	}

	private void doMain() throws Exception {

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
		List<Account> readAccounts = csvProcessor.readAll(csvFile, null);

		// print out our read in accounts
		printAccounts(readAccounts);
		compareAccounts(accounts, readAccounts);
	}

	private List<Account> createFakeAccounts() {
		Account account1 = new Account("Bill Smith", 1, 123123.34);
		Account account2 = new Account("Foo Bar", 2, 0.12);
		Account account3 = new Account("Jim Jimston", 3, -5125640.00);
		List<Account> accounts = Arrays.asList(account1, account2, account3);
		return accounts;
	}

	private void printAccounts(List<Account> readAccounts) {
		for (Account account : readAccounts) {
			System.out.println(account.name + "," + account.number + "," + account.amount);
		}
	}

	private void compareAccounts(List<Account> accounts1, List<Account> accounts2) {
		assertEquals(accounts1.size(), accounts2.size());
		for (int i = 0; i < accounts1.size(); i++) {
			assertTrue(accounts1.get(i).isSame(accounts2.get(i)));
		}
	}

	private void printFile(File csvFile) throws FileNotFoundException, IOException {
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
			/*
			 * For simple-csv which needs to be able to construct these entities.
			 */
		}

		public Account(String name, long number, double amount) {
			this.name = name;
			this.number = number;
			this.amount = amount;
		}

		public boolean isSame(Account other) {
			return this.name.equals(other.name) && this.number == other.number && this.amount == other.amount;
		}
	}
}
