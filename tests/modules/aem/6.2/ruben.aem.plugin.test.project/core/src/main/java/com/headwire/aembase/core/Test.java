package com.headwire.aembase.core;

import java.util.Map;

import com.cedarsoftware.util.io.*;

public class Test {

	public static void main(String[] args) {

		Map map = (JsonReader.jsonToMaps("{\"value\": \"hello\"}"));
		System.out.println(map);
	}

}
