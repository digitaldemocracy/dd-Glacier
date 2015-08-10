package glacier;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.glacier.AmazonGlacierClient;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sqs.AmazonSQSClient;

public class Vault {
    public static String vaultName;
    
    public static AmazonGlacierClient client;

    public static ProfileCredentialsProvider credentials;
    
    public static AmazonSQSClient sqsClient;
    
    public static AmazonSNSClient snsClient;
        
    public Vault(String region, String vault) {
    	credentials = new ProfileCredentialsProvider();
        client = new AmazonGlacierClient(credentials);
        client.setEndpoint("https://glacier."+region+".amazonaws.com");
        sqsClient = new AmazonSQSClient(credentials);
        snsClient = new AmazonSNSClient(credentials);
        
        sqsClient.setEndpoint("sqs."+region+".amazonaws.com");
        snsClient.setEndpoint("sns."+region+".amazonaws.com");

        vaultName = vault;
    }
}
