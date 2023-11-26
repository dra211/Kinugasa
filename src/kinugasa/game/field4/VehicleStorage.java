/*
 * Copyright (C) 2023 Shinacho
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package kinugasa.game.field4;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import kinugasa.game.GameLog;
import kinugasa.game.system.GameSystem;
import kinugasa.resource.Storage;
import kinugasa.resource.text.FileIOException;
import kinugasa.resource.FileNotFoundException;
import kinugasa.resource.text.IllegalXMLFormatException;
import kinugasa.resource.text.XMLElement;
import kinugasa.resource.text.XMLFile;
import kinugasa.resource.text.XMLFileSupport;

/**
 *
 * @vesion 1.0.0 - 2021/11/25_7:10:13<br>
 * @author Shinacho<br>
 */
public class VehicleStorage extends Storage<Vehicle> implements XMLFileSupport {

	private static final VehicleStorage INSTANCE = new VehicleStorage();

	public static VehicleStorage getInstance() {
		return INSTANCE;
	}

	private VehicleStorage() {
	}

	@Override
	public void readFromXML(String filePath) throws IllegalXMLFormatException, FileNotFoundException, FileIOException {
		XMLFile file = new XMLFile(filePath);
		if (!file.getFile().exists()) {
			throw new FileNotFoundException(file + " is not found");
		}
		XMLElement root = file.load().getFirst();

		for (XMLElement e : root.getElement("vehicle")) {
			String name = e.getAttributes().get("name").getValue();
			float speed = e.getAttributes().get("speed").getFloatValue();

			List<MapChipAttribute> list = new ArrayList<>();
			for (XMLElement ee : e.getElement("stepOn")) {
				list.add(MapChipAttributeStorage.getInstance().get(ee.getAttributes().get("name").getValue()));
			}
			add(new Vehicle(name, speed, list));
		}
		if (GameSystem.isDebugMode()) {
			GameLog.print(getAll());
		}
	}

	private Vehicle currentVehicle;

	public Vehicle getCurrentVehicle() {
		return currentVehicle;
	}

	public void setCurrentVehicle(String name) {
		this.currentVehicle = get(name);
	}

}
