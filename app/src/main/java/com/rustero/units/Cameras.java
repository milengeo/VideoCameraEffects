package com.rustero.units;


import android.hardware.Camera;

import com.rustero.App;
import com.rustero.tools.Caminf;
import com.rustero.tools.Lmx;
import com.rustero.tools.Size2D;
import com.rustero.tools.Tools;

public class Cameras {




	public static void findCameras() {
		// first run, detect cameras
		Lmx xml = new Lmx();
		int camId;
		Camera frontCamera = null;
		camId = Caminf.findFrontCamera();
		if (camId > -1)
			frontCamera = Camera.open(camId);
		if (null != frontCamera) {
			App.setPrefBln("now_front", true);
			App.gFrontCam.resolutions = Caminf.getCameraSizes(frontCamera);
			frontCamera.release();
			//App.log("front camera sizes");
			packCameraSizes(xml, App.gFrontCam);
			xml.pushNode("front_sizes");

			Size2D s2 = App.gFrontCam.resolutions.getBelowHeight(777);
			String defres = s2.toText();
			App.setPrefStr("front_resolution", defres);
		}

		Camera backCamera = null;
		camId = Caminf.findBackCamera();
		if (camId > -1)
			backCamera = Camera.open(camId);
		if (null != backCamera) {
			App.setPrefBln("now_front", false);
			App.gBackCam.resolutions = Caminf.getCameraSizes(backCamera);
			backCamera.release();
			//App.log("back camera sizes");
			packCameraSizes(xml, App.gBackCam);
			xml.pushNode("back_sizes");

			Size2D s2 = App.gBackCam.resolutions.getBelowHeight(777);
			String defres = s2.toText();
			App.setPrefStr("back_resolution", defres);
		}

		xml.pushNode("cameras");
		final String code = xml.getCode();
		Tools.writePrivateFile(App.self, App.CAMERAS_XML, code);

		readCameras(code);
	}



	public static void packCameraSizes(Lmx aXml, Caminf aCaminf) {
		aCaminf.resolutions.sort();
		for (int i=0; i<aCaminf.resolutions.length(); i++) {
			Size2D size = aCaminf.resolutions.get(i);
			App.log("cam size: " + size.x + "x" + size.y);
			aXml.addInt("width", size.x);
			aXml.addInt("height", size.y);
			aXml.pushItem("item");
		}
	}



	public static void readCameras(String aCode) {

		try {
			Lmx xml = new Lmx(aCode);
			xml.pullNode("front_sizes");
			if (!xml.isEmpty()) {
				readCameraSizes(xml, App.gFrontCam);
				App.gFrontResoList = new String[App.gFrontCam.resolutions.length()];
				for (int i=0; i<App.gFrontCam.resolutions.length(); i++) {
					Size2D s2 = App.gFrontCam.resolutions.get(i);
					App.gFrontResoList[i] = s2.toText();
				}
			} else
				App.gFrontCam = null;

			xml.pullNode("back_sizes");
			if (!xml.isEmpty()) {
				readCameraSizes(xml, App.gBackCam);
				App.gBackResoList = new String[App.gBackCam.resolutions.length()];
				for (int i=0; i<App.gBackCam.resolutions.length(); i++) {
					Size2D s2 = App.gBackCam.resolutions.get(i);
					App.gBackResoList[i] = s2.toText();
				}
			} else
				App.gBackCam = null;
		} catch (Exception ex) {
			App.log(" *** " + ex.getMessage());
		}
	}



	public static void readCameraSizes(Lmx aXml, Caminf aCaminf) {
		aCaminf.resolutions.clear();
		while (true) {
			aXml.pullItem("item");
			if (aXml.isEmpty()) break;
			int width = aXml.getInt("width");
			int height = aXml.getInt("height");
			aCaminf.resolutions.addSize(new Size2D(width, height));
		}
	}




}
