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

import java.util.*;

import franklinmath.util.*;

/**
 *
 * @author Allen Jordan
 */
public final class SymbolicFunction implements LatexOutput {

    final private String name;
    final private Vector<Equation> paramList;
    final private boolean isMathFunction;

    public SymbolicFunction() {
        name = "unknown";
        paramList = new Vector<Equation>();
        isMathFunction = true;
    }

    public SymbolicFunction(String nameStr, Vector<Equation> list, boolean isMathFunc) {
        name = nameStr;
        paramList = (Vector<Equation>) list.clone();
        isMathFunction = isMathFunc;
    }

    public String GetName() {
        return name;
    }

    public Vector<Equation> GetParamList() {
        return (Vector<Equation>) paramList.clone();
    }

    public boolean IsMathFunction() {
        return isMathFunction;
    }

    public String toLatexString() {
        return "";
    }

    @Override
    public String toString() {
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append(name);
        strBuilder.append("[");
        for (int i = 0; i < paramList.size(); i++) {
            if (i != 0) {
                strBuilder.append(", ");
            }
            strBuilder.append(paramList.get(i).toString());
        }
        strBuilder.append("]");
        return strBuilder.toString();
    }

    @Override
    public boolean equals(Object obj) {
        //check for equal reference values
        if (this == obj) {
            return true;
        }

        //if this is not a math function, always return false
        if (!isMathFunction) {
            return false;
        }

        SymbolicFunction compareFunction = (SymbolicFunction) obj;
        if (!compareFunction.GetName().equals(name)) {
            return false;
        }

        Vector<Equation> compareParamList = compareFunction.GetParamList();
        int listSize = paramList.size();
        for (int i = 0; i < listSize; i++) {
            if (!compareParamList.get(i).equals(paramList.get(i))) {
                return false;
            }
        }

        return true;
    }

    @Override
    public int hashCode() {
        int primeNumber = 7;
        int hash = 1;

        hash = hash * primeNumber + name.hashCode();
        int listSize = paramList.size();
        for (int i = 0; i < listSize; i++) {
            hash = hash * primeNumber + paramList.get(i).hashCode();
        }

        return hash;
    }
}
