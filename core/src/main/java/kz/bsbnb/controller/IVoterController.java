package kz.bsbnb.controller;


import kz.bsbnb.util.SimpleResponse;

/**
 * Created by serik.mukashev on 26.12.2017.
 */
public interface IVoterController {

    SimpleResponse getVoterById(Long voterId);

    SimpleResponse getVoter(Long votingId);

    SimpleResponse canVote(Long voterId);
}
