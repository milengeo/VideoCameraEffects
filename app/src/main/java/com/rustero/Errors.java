package com.rustero;

public class Errors {



	public static String getText(int aCode) {
		String result = "";
		switch (aCode) {

			case CANCELLED:     			result = "Cancelled"; break;
			case UNKNOWN_ERROR:   			result = "Unknown error"; break;
			case NO_MORE_DATA:    			result = "No more bufData"; break;
			case CORRUPTED_DATA: 	 		result = "Corrupted bufData"; break;
			case WRONG_PASSWORD:  			result = "Wrong password"; break;

			case OPEN_FILE:     			result = "Error opening file!"; break;
			case CREATE_FILE:     			result = "Error creating file!"; break;
			case WRITE_FILE:     			result = "Error writing file!"; break;
			case READ_FILE:     			result = "Error reading file!"; break;

			case CONNECT_URL:      			result = "Cannot connect to URL!"; break;
			case GETTING_SIZE:     			result = "Cannot get remote file size!"; break;
			case PARTIAL_REQUESTS: 			result = "Error getting HTTP partial requests!"; break;
			case NOT_DIRECT_URL: 		    result = "Web address is not a direct download URL!"; break;

			case UNKNOWN_FILE_FORMAT:   	result = "Unknown file format"; break;
			case UNKNOWN_VIDEO_FORMAT:   	result = "Unknown video format"; break;
			case UNKNOWN_AUDIO_FORMAT:   	result = "Unknown audio format"; break;
		}
		return result;
	}

	public static final int CANCELLED =                	-1;
	public static final int UNKNOWN_ERROR =            	-2;
	public static final int NO_MORE_DATA =             	-3;
	public static final int CORRUPTED_DATA =          	-4;
	public static final int WRONG_PASSWORD = 			-5;

	public static final int OPEN_FILE = 				-10;
	public static final int CREATE_FILE =             	-11;
	public static final int WRITE_FILE =              	-12;
	public static final int READ_FILE =              	-13;

	public static final int CONNECT_URL = 				-20;
	public static final int GETTING_SIZE =				-21;
	public static final int PARTIAL_REQUESTS =			-22;
	public static final int NOT_DIRECT_URL =			-23;

	public static final int UNKNOWN_FILE_FORMAT = 	    -30;
	public static final int UNKNOWN_VIDEO_FORMAT =   	-31;
	public static final int UNKNOWN_AUDIO_FORMAT =    	-32;




}
