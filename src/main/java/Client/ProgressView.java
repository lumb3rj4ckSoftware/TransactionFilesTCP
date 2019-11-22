package Client;

public interface ProgressView {
	void setNumberOfFiles(int numberOfFiles);
	void setTotalDataAmount(double dataAmount);
	void decNumberOfFiles();
	void decNumberOfData(double dataRead);
	int getCurrentProgress();
	int getNumberOfFiles();
	double getCurrentDataProgress();
	double getTotalDataAmount();
}
