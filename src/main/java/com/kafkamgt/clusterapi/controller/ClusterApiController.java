package com.kafkamgt.clusterapi.controller;

import com.kafkamgt.clusterapi.services.ManageKafkaComponents;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/topics")
@Slf4j
public class ClusterApiController {

    @Autowired
    ManageKafkaComponents manageKafkaComponents;

    @RequestMapping(value = "/getApiStatus", method = RequestMethod.GET,produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> getApiStatus(){
        return new ResponseEntity<>("ONLINE", HttpStatus.OK);
    }

    @RequestMapping(value = "/getStatus/{env}/{protocol}", method = RequestMethod.GET,produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> getStatus(@PathVariable String env, @PathVariable String protocol){
        String envStatus = manageKafkaComponents.getStatus(env, protocol);

        return new ResponseEntity<>(envStatus, HttpStatus.OK);
    }

    @RequestMapping(value = "/getTopics/{env}/{protocol}", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<Set<HashMap<String,String>>> getTopics(@PathVariable String env, @PathVariable String protocol){
        Set<HashMap<String,String>> topics = manageKafkaComponents.loadTopics(env, protocol);
        return new ResponseEntity<>(topics, HttpStatus.OK);
    }

    @RequestMapping(value = "/getAcls/{env}/{protocol}", method = RequestMethod.GET,produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<Set<HashMap<String,String>>> getAcls(@PathVariable String env, @PathVariable String protocol){
        Set<HashMap<String,String>> acls = manageKafkaComponents.loadAcls(env, protocol);

        return new ResponseEntity<>(acls, HttpStatus.OK);
    }

    @PostMapping(value = "/createTopics")
    public ResponseEntity<String> createTopics(@RequestBody MultiValueMap<String, String> topicRequest){
        try {
            manageKafkaComponents.createTopic(
                    topicRequest.get("topicName").get(0),
                    topicRequest.get("partitions").get(0),
                    topicRequest.get("rf").get(0),
                    topicRequest.get("env").get(0),
                    topicRequest.get("protocol").get(0)
                );
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("failure "+e, HttpStatus.OK);
        }

        return new ResponseEntity<>("success", HttpStatus.OK);
    }

    @PostMapping(value = "/deleteTopics")
    public ResponseEntity<String> deleteTopics(@RequestBody MultiValueMap<String, String> topicRequest){
        try {
            manageKafkaComponents.deleteTopic(
                    topicRequest.get("topicName").get(0),
                    topicRequest.get("env").get(0),
                    topicRequest.get("protocol").get(0)
            );
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("failure "+e, HttpStatus.OK);
        }

        return new ResponseEntity<>("success", HttpStatus.OK);
    }

    @PostMapping(value = "/createAcls")
    public ResponseEntity<String> createAcls(@RequestBody MultiValueMap<String, String> topicRequest){

        String result;
        try {
            String aclType = topicRequest.get("aclType").get(0);

            if (aclType.equals("Producer"))
                result = manageKafkaComponents.updateProducerAcl(topicRequest.get("topicName").get(0),
                        topicRequest.get("env").get(0), topicRequest.get("protocol").get(0),
                        topicRequest.get("acl_ip").get(0), topicRequest.get("acl_ssl").get(0), "Create");
            else
                result = manageKafkaComponents.updateConsumerAcl(topicRequest.get("topicName").get(0),
                        topicRequest.get("env").get(0), topicRequest.get("protocol").get(0),
                        topicRequest.get("acl_ip").get(0), topicRequest.get("acl_ssl").get(0),
                        topicRequest.get("consumerGroup").get(0), "Create");

            return new ResponseEntity<>(result, HttpStatus.OK);
        }catch(Exception e){
            return new ResponseEntity<>("failure "+e.getMessage(), HttpStatus.OK);
        }
    }

    @PostMapping(value = "/deleteAcls")
    public ResponseEntity<String> deleteAcls(@RequestBody MultiValueMap<String, String> topicRequest){
        String result;
        try {
            String aclType = topicRequest.get("aclType").get(0);

            if (aclType.equals("Producer"))
                result = manageKafkaComponents.updateProducerAcl(topicRequest.get("topicName").get(0),
                        topicRequest.get("env").get(0), topicRequest.get("protocol").get(0),
                        topicRequest.get("acl_ip").get(0), topicRequest.get("acl_ssl").get(0), "Delete");
            else
                result = manageKafkaComponents.updateConsumerAcl(topicRequest.get("topicName").get(0),
                        topicRequest.get("env").get(0), topicRequest.get("protocol").get(0),
                        topicRequest.get("acl_ip").get(0), topicRequest.get("acl_ssl").get(0),
                        topicRequest.get("consumerGroup").get(0), "Delete");

            return new ResponseEntity<>(result, HttpStatus.OK);
        }catch(Exception e){
            return new ResponseEntity<>("failure " + e.getMessage(), HttpStatus.OK);
        }
    }

    @PostMapping(value = "/postSchema")
    public ResponseEntity<String> postSchema(@RequestBody MultiValueMap<String, String> fullSchemaDetails){
        try {
            String topicName = fullSchemaDetails.get("topicName").get(0);
            String schemaFull = fullSchemaDetails.get("fullSchema").get(0);
            String env = fullSchemaDetails.get("env").get(0);

            String result = manageKafkaComponents.postSchema(topicName, schemaFull, env);
            return new ResponseEntity<>("Status:"+result, HttpStatus.OK);
        }catch(Exception e){
            return new ResponseEntity<>("failure "+e.getMessage(), HttpStatus.OK);
        }
    }


}
