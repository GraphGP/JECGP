package evaluation;
/**
 * Class for interpreting the output of the Individual
 * @author Björn Piepenbrink
 *
 */
public class ComputeOutput {
	/**
	 * Method for interpreting the Ouput of the Individual
	 * @param output Output of the Individual
	 * @param classification The Classification that should be reached
	 * @return 1 if Classification correct (0 else)
	 * @throws Exception if output cannot be computed
	 */
	public static int computeOutputAndClassification(double[] output, int classification) throws Exception {
		if (output.length == 1) {
			if ((int) Math.round(output[0]) == classification) {
				return 0;
			} else {
				return 1;
			}
		} else if (output.length == 4) {
			int outputInInteger = -1;
			// check Outputs
			for (int i = 0; i < output.length; i++) {
				if (output[i] != 0 && output[i] != 1) {
					throw new Exception("Only Outputs of 1 and 0 are allowed if the output length is 4");
				}
			}
			// Binary Output to Int Input
			if (output[0] == 0) {
				if (output[1] == 0) {
					if (output[2] == 0) {
						if (output[3] == 0) {
							outputInInteger = 0;
						} else {
							outputInInteger = 1;
						}
					} else {
						if (output[3] == 0) {
							outputInInteger = 2;
						} else {
							outputInInteger = 3;
						}
					}
				} else {
					if (output[2] == 0) {
						if (output[3] == 0) {
							outputInInteger = 4;
						} else {
							outputInInteger = 5;
						}
					} else {
						if (output[3] == 0) {
							outputInInteger = 6;
						} else {
							outputInInteger = 7;
						}
					}
				}
			} else {
				if (output[1] == 0 && output[2] == 0) {
					if (output[3] == 0) {
						outputInInteger = 8;
					} else {
						outputInInteger = 9;
					}
				}
			}

			if (outputInInteger == classification) {
				return 0;
			} else {
				return 1;
			}

		} else {
			throw new Exception("Output Length of " + output.length + " is unknown and cannot be further processed\n"
					+ "additional Code in FitnessCalculator is needed for calcultaing Fitness");
		}
	}
}
