package kz.bsbnb.controller;

import kz.bsbnb.common.bean.*;
import kz.bsbnb.common.model.*;
import kz.bsbnb.util.SimpleResponse;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * Created by kanattulbassiyev on 8/7/16.
 */
public interface IUserController {
    List<UserBean> getUsers(int page, int count);

    User getUserById(Long id);

    SimpleResponse getUserByIdSimple(Long id);

    User regUser(User user);

    SimpleResponse checkUser(User user);

    //Смена пароля
    SimpleResponse updateUser(User user);

    SimpleResponse deleteUser(User user);

    List<Organisation> getAllOrgs(Long userId);

    List<OrgBean> getAllOrgsWithWorkVoting(Long userId);

    List<OrgBean> getAllOrgsWithOldVoting(Long userId);

    UserBean castUser(User user);

    SimpleResponse complateVoting(Long votingId, Long userId, ConfirmBean bean);

    List<QuestionBean> getVotingQuestions(Long votingId, Long userId);

    QuestionBean castFromQuestion(Question q);

    boolean canVote(Voting voting, User user);

    VoterBean castToBean(Voting voting, Voter voter);

    DecisionBean getBeanFromDecision(Decision decision);

    void getVotingQuestions(String fileCode,
                            HttpServletResponse response);

    List<Files> getVotingQuestionFiles(Long votingId, Long questionId);

    VotingBean castToBean(Voting voting, User user);
}
