package kz.bsbnb.controller;

import kz.bsbnb.common.bean.*;
import kz.bsbnb.common.model.Answer;
import kz.bsbnb.common.model.Decision;
import kz.bsbnb.common.model.Question;
import kz.bsbnb.common.model.Voting;
import kz.bsbnb.util.SimpleResponse;

import javax.servlet.http.HttpServletResponse;
import java.text.ParseException;
import java.util.List;

/**
 * Created by ruslan on 20/10/2016.
 */
public interface IVotingController {

    List<Voting> getVotings(Long userId, int page, int count);

    List<Voting> getWorkVotings(Long userId, int page, int count);

    List<Voting> getOldVotings(Long userId, int page, int count);

    List<QuestionBean> getVotingQuestions(Long votingId, int page, int count);

    QuestionBean getVotingQuestion(Long votingId, Long qid);

    SimpleResponse addVotingQuestions(Long votingId, RegQuestionBean question);

    SimpleResponse editVotingQuestions(Long votingId, RegQuestionBean question);

    SimpleResponse deleteVotingQuestions(Long votingId, Question question);

    SimpleResponse addVotingAnswer(Long votingId, Answer answer);

    SimpleResponse editVotingAnswer(Long votingId, Answer answer);

    SimpleResponse deleteVotingAnswer(Long votingId, Answer answer);

    List<QuestionBean> getVotingQuestions(Long votingId, Long userId);

    Decision getDecisionFromBean(DecisionBean bean) throws ParseException;

    SimpleResponse createVoting(RegVotingBean votingBean);

    SimpleResponse startVoting(Long votingId, Long userId, ConfirmBean confirmBean);

    SimpleResponse restartVoting(Long votingId, Long userId, ConfirmBean confirmBean);

    SimpleResponse stopVoting(Long votingId, Long userId, ConfirmBean confirmBean);

    SimpleResponse closeVoting(Long votingId, Long userId, ConfirmBean confirmBean);

    SimpleResponse editVoting(RegVotingBean votingBean);

    SimpleResponse addVoter(Long userId, RegVoterBean regVoterBean);

    SimpleResponse delVoter(Long votingId, Long userId);

    SimpleResponse deleteVoting(Long votingId, Long userId, ConfirmBean confirmBean);

    SimpleResponse getVoting(Long votingId);

    List<VotingBean> getReadyForOperVotings(Long userId);

    void getVotingQuestions(Long votingId, HttpServletResponse response);

    public List<Voting> getOldVotingsForUser(Long userId, int page, int count);

    SimpleResponse editVotingWhenStarted(RegVotingBean votingBean, String reason);


}
