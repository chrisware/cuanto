/*
	Copyright (c) 2010 Todd E. Wells

	This file is part of Cuanto, a test results repository and analysis program.

	This program is free software: you can redistribute it and/or modify
	it under the terms of the GNU Lesser General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.

	This program is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU Lesser General Public License for more details.

	You should have received a copy of the GNU Lesser General Public License
	along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package cuanto

public class TestProperty implements Comparable {

	String name
	String value

	TestProperty(){}


	TestProperty(String name, String value) {
		this.name = name
		this.value = value
	}




	public int compareTo(Object t) {
		TestProperty otherProp = (TestProperty) t

		def nameComp = this.name.compareTo(otherProp.name)
		if (nameComp == 0) {
			return this.value.compareTo(other.value)
		} else {
			return nameComp
		}
	}


	public boolean equals(Object t) {
		TestProperty otherProp = (TestProperty) t
		return this.name == otherProp.name && this.value == otherProp.value
	}
}