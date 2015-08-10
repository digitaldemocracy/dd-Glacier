package glacier;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import com.amazonaws.services.glacier.transfer.ArchiveTransferManager;
import com.amazonaws.services.glacier.transfer.UploadResult;

public class ArchiveOperation {
	private Vault archive;
	private ArchiveTransferManager atm;
	
	public ArchiveOperation(Vault archive) {
		this.archive = archive;
		try {
			//this.atm = new ArchiveTransferManager(this.archive.client, this.archive.credentials);
		    this.atm = new ArchiveTransferManager(this.archive.client, this.archive.sqsClient, this.archive.snsClient);

		} catch (Exception e) {
            System.err.println(e);
        }
	}

	public String upload(String fileName)  throws IOException {
		String archiveId = null;
		try {
			UploadResult result = atm.upload(archive.vaultName, "Archive " + (new Date()), new File(fileName));
			archiveId = result.getArchiveId();
		} catch(Exception e) {
			System.err.println(e);
		}
		return archiveId;
	}
	
	public int download(String archiveId, String path) {
		int result = 0;

        try {
            
            this.atm.download(archive.vaultName, archiveId, new File(path));
            System.out.println("Downloaded file to " + path);
            result = 1;
        } catch (Exception e)
        {
            System.err.println(e);
        }

		return result;
	}
	
	public static void main(String[] args) throws IOException {
		String fileName = "/Users/toshihirokuboi/Downloads/Apr 10 01 Sen Bud Sub #4 Oversight on Tech and Implementation.mpg";
		String region = "us-west-2";
		String vaultName = "2014_Digital_Democracy";
		if (args.length > 1) {
			fileName = args[1];
			if (args.length > 2) {
				vaultName = args[2];
			}
		} else {
			System.out.println("Usage: glacier.ArchiveOperation.upload fileName <vault_name>");
			//return;
		}
		Vault archive = new Vault(region, vaultName);
		ArchiveOperation archiveOperation = new ArchiveOperation(archive);
		//String archiveId = archiveOperation.upload(fileName);
		//System.out.println(archiveId + " = " + fileName);
		String path = "/Users/toshihirokuboi/Dropbox/";
		System.out.println(archiveOperation.download("aYmefH5-AFobwVhk007ZzyoZJlXLRPBnc1U8z9-vhhDoq9L4yt4b3zCSP0ir-buBaTVzWcCGB773BAKoaQa0WIa55jii5GxqUno1ZOOgZKom7U5fLupH-ehKqXlRTe5-KN2y2pgQqA",path));
	}
}
