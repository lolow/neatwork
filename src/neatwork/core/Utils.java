package neatwork.core;

import java.util.Arrays;
import java.util.List;

public class Utils {

    public static double[] addArrays(double[] array1, double[] array2) {
        int length = Math.min(array1.length, array2.length);
        double[] result = new double[length];

        for (int i = 0; i < length; i++) {
            result[i] = array1[i] + array2[i];
        }

        return result;
    }

    public static int[][] removeColumns(int[][] matrix, List<Integer> columnsToRemove) {
        int numRows = matrix.length;
        int numColumns = matrix[0].length - columnsToRemove.size();
        int[][] newMatrix = new int[numRows][numColumns];

        for (int i = 0; i < numRows; i++) {
            int destColumnIndex = 0;
            for (int srcColumnIndex = 0; srcColumnIndex < matrix[0].length; srcColumnIndex++) {
                if (!columnsToRemove.contains(srcColumnIndex)) {
                    newMatrix[i][destColumnIndex++] = matrix[i][srcColumnIndex];
                }
            }
        }

        return newMatrix;
    }

    public static double[] removeElements(double[] array, List<Integer> positionsToRemove) {
        return removeElements(array, positionsToRemove, 0);
    }

    public static double[] removeElements(double[] array, List<Integer> positionsToRemove, int offset) {
        int newArrayLength = array.length - positionsToRemove.size();
        double[] newArray = new double[newArrayLength];

        int destIndex = 0;
        for (int srcIndex = 0; srcIndex < array.length; srcIndex++) {
            if (!positionsToRemove.contains(srcIndex - offset)) {
                newArray[destIndex++] = array[srcIndex];
            }
        }

        return newArray;
    }

    public static double[][] keepColumns(double[][] matrix, List<Integer> columnsToKeep) {
        int numRows = matrix.length;
        int numColumns = columnsToKeep.size();
        double[][] newMatrix = new double[numRows][numColumns];

        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < numColumns; j++) {
                int columnIndex = columnsToKeep.get(j);
                newMatrix[i][j] = matrix[i][columnIndex];
            }
        }

        return newMatrix;
    }

    public static double[] calculateColumnSums(double[][] matrix) {
        int numRows = matrix.length;
        int numCols = matrix[0].length;

        double[] columnSums = new double[numCols];

        for (int j = 0; j < numCols; j++) {
            for (int i = 0; i < numRows; i++) {
                columnSums[j] += matrix[i][j];
            }
        }

        return columnSums;
    }

    public static double computeMean(double[] array, List<Integer> indices) {
        double sum = 0.0;
        for (int index : indices) {
            sum += array[index];
        }
        return sum / indices.size();
    }

    public static double computeStd(double[] array, List<Integer> indices) {
        double mean = computeMean(array, indices);
        double sumSquaredDiff = 0.0;

        for (int index : indices) {
            double diff = array[index] - mean;
            sumSquaredDiff += diff * diff;
        }

        return Math.sqrt(sumSquaredDiff / indices.size());
    }

    public static double computePercentile(double[] array, int percentile) {
        if (array.length == 0 || percentile < 0 || percentile > 100) {
            throw new IllegalArgumentException("Invalid input");
        }

        Arrays.sort(array);

        double rank = (percentile / 100.0) * (array.length - 1) + 1;

        // Si le rang n'est pas un entier : interpolation
        int lowerIndex = (int) Math.floor(rank);
        int upperIndex = (int) Math.ceil(rank);

        if (lowerIndex == upperIndex) {
            return array[lowerIndex - 1];
        } else {
            double lowerValue = array[lowerIndex - 1];
            double upperValue = array[upperIndex - 1];
            double fraction = rank - lowerIndex;
            return lowerValue + fraction * (upperValue - lowerValue);
        }
    }

    public static int findMin(int[] array) {
        int min = array[0];
        for (int value : array) {
            min = Math.min(min, value);
        }
        return min;
    }

    public static double findMean(int[] array) {
        double sum = 0.0;
        for (int value : array) {
            sum += value;
        }
        return sum / array.length;
    }

    public static int findMax(int[] array) {
        int max = array[0];
        for (int value : array) {
            max = Math.max(max, value);
        }
        return max;
    }

    public static double[][] transposeMatrix(double[][] matrix) {
        int rows = matrix.length;
        int cols = matrix[0].length;
        double[][] result = new double[cols][rows];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                result[j][i] = matrix[i][j];
            }
        }
        return result;
    }

    public static int[][] transposeMatrix(int[][] matrix) {
        int rows = matrix.length;
        int cols = matrix[0].length;
        int[][] result = new int[cols][rows];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                result[j][i] = matrix[i][j];
            }
        }
        return result;
    }

    public static double[][] convertIntToDoubleMatrix(int[][] intMatrix) {
        int rows = intMatrix.length;
        int cols = intMatrix[0].length;
    
        double[][] doubleMatrix = new double[rows][cols];
    
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                doubleMatrix[i][j] = (double) intMatrix[i][j];
            }
        }
    
        return doubleMatrix;
    }

    public static double[][] createDiagonalMatrix(double[] array) {
        int n = array.length;
        double[][] result = new double[n][n];
        for (int i = 0; i < n; i++) {
            result[i][i] = array[i];
        }
        return result;
    }

    public static double[][] multiplyMatrices(double[][] matrix1, double[][] matrix2) {
        int rows1 = matrix1.length;
        int cols1 = matrix1[0].length;
        int cols2 = matrix2[0].length;
        double[][] result = new double[rows1][cols2];
        for (int i = 0; i < rows1; i++) {
            for (int j = 0; j < cols2; j++) {
                for (int k = 0; k < cols1; k++) {
                    result[i][j] += matrix1[i][k] * matrix2[k][j];
                }
            }
        }
        return result;
    }

    public static double[][] addMatrices(double[][] matrix1, double[][] matrix2) {
        int rows = matrix1.length;
        int cols = matrix1[0].length;
        double[][] result = new double[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                result[i][j] = matrix1[i][j] + matrix2[i][j];
            }
        }
        return result;
    }

    public static double[][] scalarMultiplyMatrix(double[][] matrix, double scalar) {
        int rows = matrix.length;
        int cols = matrix[0].length;
        double[][] result = new double[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                result[i][j] = matrix[i][j] * scalar;
            }
        }
        return result;
    }

    public static double[][] createIdentityMatrix(int n) {
        double[][] identityMatrix = new double[n][n];
        for (int i = 0; i < n; i++) {
            identityMatrix[i][i] = 1.0;
        }
        return identityMatrix;
    }

    public static double[][] chol(double[][] A) {
        int n = A.length;
        double[][] L = new double[n][n];

        for (int i = 0; i < n; i++) {
            for (int j = 0; j <= i; j++) {
                double sum = A[i][j];
                for (int k = 0; k < j; k++) {
                    sum -= L[k][i] * L[k][j];
                }

                if (i == j) {
                    L[i][j] = Math.sqrt(Math.max(sum, 0));
                } else {
                    L[j][i] = sum / L[j][j];
                }
            }
        }

        return L;
    }

    public static double[] solveLinearSystem(double[][] A, double[][] At, double[] b) {
        // Résoudre le système Ax = b
        double[] y = solveLowerTriangularSystem(A, b);

        // Transposer A et résoudre Atx = y
        double[] x = solveUpperTriangularSystem(At, y);

        return x;
    }
    
    public static double[] solveLowerTriangularSystem(double[][] L, double[] b) {
        int n = L.length;
        double[] y = new double[n];

        for (int i = 0; i < n; i++) {
            double sum = b[i];
            for (int j = 0; j < i; j++) {
                sum -= L[j][i] * y[j];
            }
            y[i] = sum / L[i][i];
        }

        return y;
    }
    
    public static double[] solveUpperTriangularSystem(double[][] U, double[] b) {
        int n = U.length;
        double[] x = new double[n];

        for (int i = n - 1; i >= 0; i--) {
            double sum = b[i];
            for (int j = i + 1; j < n; j++) {
                sum -= U[j][i] * x[j];
            }
            x[i] = sum / U[i][i];
        }

        return x;
    }

    public static int[][] createLowerTriangularMatrix(int size) {
        int[][] matrix = new int[size][size];

        for (int i = 0; i < size; i++) {
            for (int j = 0; j <= i; j++) {
                matrix[i][j] = 1;
            }
        }

        return matrix;
    }

    // public static double[][] multiplyMatrices(int[][] A, double[][] B) {
    //     int rowsA = A.length;
    //     int colsA = A[0].length;
    //     int colsB = B[0].length;

    //     double[][] result = new double[rowsA][colsB];

    //     for (int i = 0; i < rowsA; i++) {
    //         for (int j = 0; j < colsB; j++) {
    //             for (int k = 0; k < colsA; k++) {
    //                 result[i][j] += A[i][k] * B[k][j];
    //             }
    //         }
    //     }

    //     return result;
    // }


    public static double[][] multiplyMatrices(int[][] A, double[][] B) {
        // Si les dimensions sont identiques, effectuer le produit de Hadamard
        if (A.length == B.length && A[0].length == B[0].length) {
            int rows = A.length;
            int cols = A[0].length;
            double[][] result = new double[rows][cols];
    
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    result[i][j] = A[i][j] * B[i][j];
                }
            }
            return result;
        } 
        // Sinon, effectuer le produit matriciel standard
        else {
            int rowsA = A.length;
            int colsA = A[0].length;
            int colsB = B[0].length;
    
            double[][] result = new double[rowsA][colsB];
    
            for (int i = 0; i < rowsA; i++) {
                for (int j = 0; j < colsB; j++) {
                    for (int k = 0; k < colsA; k++) {
                        result[i][j] += A[i][k] * B[k][j];
                    }
                }
            }
            return result;
        }
    }
    




    
    public static double[][] multiplyElementwise(double[] A, int[][] B) {
        int rowsB = B.length;
        int colsB = B[0].length;

        double[][] result = new double[rowsB][colsB];

        for (int i = 0; i < rowsB; i++) {
            for (int j = 0; j < colsB; j++) {
                result[i][j] = A[i] * B[i][j];
            }
        }

        return result;
    }
    
    public static double[] findMaxValuesOfEachRow(double[][] matrix) {
        int rows = matrix.length;
        int cols = matrix[0].length;
    
        double[] maxValues = new double[rows];
    
        for (int i = 0; i < rows; i++) {
            double max = matrix[i][0];
            for (int j = 1; j < cols; j++) {
                if (matrix[i][j] > max) {
                    max = matrix[i][j];
                }
            }
            maxValues[i] = max;
        }
    
        return maxValues;
    }



    // Méthode pour multiplier un vecteur avec une matrice transposée
    public static double[] multiplyVectorAndMatrixTransposed(int[][] matrix, double[] vector) {
        // Vérification des dimensions pour la compatibilité
        if (matrix.length != vector.length) {
            throw new IllegalArgumentException("Le nombre de lignes de la matrice doit correspondre à la taille du vecteur.");
        }

        int rows = matrix.length;
        int cols = matrix[0].length;
        double[] result = new double[cols];
        
        // Multiplication du vecteur par la matrice transposée
        for (int j = 0; j < cols; j++) {
            double sum = 0.0;
            for (int i = 0; i < rows; i++) {
                sum += matrix[i][j] * vector[i]; // Multiplie chaque élément du vecteur avec l'élément correspondant de la matrice transposée
            }
            result[j] = sum;
        }

        return result;
    }

}
