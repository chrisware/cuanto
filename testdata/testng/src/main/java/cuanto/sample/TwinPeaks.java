/*

 Copyright (c) 2010 thePlatform for Media, Inc.

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

package cuanto.sample;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

/**
 * Created by IntelliJ IDEA. User: todd.wells Date: Apr 12, 2010 Time: 12:43:31 PM
 */
public class TwinPeaks {

	@Test(groups = {"quirks"})
	public void tibetanMethod() {

	}

    @Test(groups = {"quirks"})
    public void giantVisions() {
        fail("It is happening again");
    }

    @Test(groups = {"places"})
    public void greatNorthern() {

    }

    @Test(groups = {"places", "quirks"})
    public void blackLodge()
    {
        fail("Let's rock!");
    }

    @Test(groups = {"weirdos"})
    public void logLady() {

    }

    @Test(groups = {"weirdos"})
    public void sarahPalmer() {

    }
}