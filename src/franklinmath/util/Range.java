/*
Copyright 2009 Allen Franklin Jordan (allen.jordan@gmail.com).

This file is part of Franklin Math.

Franklin Math is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Franklin Math is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Franklin Math.  If not, see <http://www.gnu.org/licenses/>.
*/

package franklinmath.util;

/**
 * This class represents a range of numbers between two endpoints.  
 * @author Allen Jordan
 */
public class Range {
    public double low, high;
    
    public Range(double low, double high) {
        assert low <= high;
        this.low = low;
        this.high = high;
    }
    
    public double GetWidth() {
        return high-low;
    }
    
    public static Range BAD_RANGE = new Range(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
    
    @Override
    public String toString() {
        return low + " <--> " + high;
    }
}
