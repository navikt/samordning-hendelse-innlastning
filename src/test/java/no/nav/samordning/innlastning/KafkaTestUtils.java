package no.nav.samordning.innlastning;

import org.apache.kafka.common.acl.AccessControlEntry;
import org.apache.kafka.common.acl.AclBinding;
import org.apache.kafka.common.acl.AclOperation;
import org.apache.kafka.common.acl.AclPermissionType;
import org.apache.kafka.common.resource.PatternType;
import org.apache.kafka.common.resource.ResourcePattern;
import org.apache.kafka.common.resource.ResourceType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class KafkaTestUtils {

    public static List<AclBinding> createProducerAcl(Map<String, String> topicToUser) {

        List<AclBinding> aclBindings = new ArrayList<>();
        List<AclOperation> legalOperations = Arrays.asList(AclOperation.DESCRIBE, AclOperation.WRITE, AclOperation.CREATE);

        for(AclOperation operation: legalOperations) {
            ResourcePattern topicPattern = new ResourcePattern(ResourceType.TOPIC, KafkaConfiguration.SAMORDNING_HENDELSE_TOPIC, PatternType.LITERAL);
            String principal = "User:" + topicToUser.get(KafkaConfiguration.SAMORDNING_HENDELSE_TOPIC);
            String host = "*";
            AccessControlEntry accessControlEntry = new AccessControlEntry(principal, host, operation, AclPermissionType.ALLOW);
            aclBindings.add(new AclBinding(topicPattern, accessControlEntry));
        }

        return aclBindings;
    }

    public static List<AclBinding> createConsumerAcl(Map<String, String> topicToUser) {

        List<AclBinding> aclBindings = new ArrayList<>();
        List<AclOperation> legalOperations = Arrays.asList(AclOperation.DESCRIBE, AclOperation.READ);
        ResourcePattern topicPattern = new ResourcePattern(ResourceType.TOPIC, KafkaConfiguration.SAMORDNING_HENDELSE_TOPIC, PatternType.LITERAL);
        ResourcePattern groupPattern = new ResourcePattern(ResourceType.GROUP, "*", PatternType.LITERAL);
        String principal = "User:" + topicToUser.get(KafkaConfiguration.SAMORDNING_HENDELSE_TOPIC);
        String host = "*";

        for(AclOperation operation: legalOperations) {
            aclBindings.add(new AclBinding(topicPattern, new AccessControlEntry(principal, host, operation, AclPermissionType.ALLOW)));
        }

        aclBindings.add(new AclBinding(groupPattern, new AccessControlEntry(principal, host, AclOperation.READ, AclPermissionType.ALLOW)));

        return aclBindings;
    }

}
