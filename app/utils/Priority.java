package utils;

public enum Priority {
	High {
		@Override
		String priorityName() {
			return "High";
		}
	},
	Normal {
		@Override
		String priorityName() {
			return "Normal";
		}
	},
	Low {
		@Override
		String priorityName() {
			return "Low";
		}
	},
	;
	String priorityName() {
		throw new UnsupportedOperationException();
	}

	final public static String allString = "All";
}
