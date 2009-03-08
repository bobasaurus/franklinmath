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

package franklinmath.expression;

/**
 * A simple class representing an immutable "single" expression that contains only one factor and its sign
 * @author Allen Jordan
 */
public final class SingleExpression {
    final private Factor singleFactor;
    final private boolean isFactorNegative;
    
    public SingleExpression(Factor factor, boolean isNegative) {
        singleFactor = factor;
        isFactorNegative = isNegative;
    }
    
    public Factor SingleValue() {
        return singleFactor;
    }
    
    public boolean IsSingleNegative() {
        return isFactorNegative;
    }
}
