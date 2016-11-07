package kz.bsbnb.controller.impl;

import kz.bsbnb.common.bean.*;
import kz.bsbnb.common.consts.Role;
import kz.bsbnb.common.model.*;
import kz.bsbnb.controller.IUserController;
import kz.bsbnb.processor.UserProcessor;
import kz.bsbnb.repository.*;
import kz.bsbnb.security.ConfirmationService;
import kz.bsbnb.util.SimpleResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;


/**
 * Created by kanattulbassiyev on 8/7/16.
 * Updated by Olzhas.Pazyldayev on 23.08.2016
 */
@RestController
@RequestMapping(value = "/user")
public class UserControllerImpl implements IUserController {
    @Autowired
    private IUserRepository userRepository;
    @Autowired
    private UserProcessor userProcessor;
    @Autowired
    private IVotingRepository votingRepository;
    @Autowired
    private ConfirmationService confirmationService;
    @Autowired
    private IVoterRepository voterRepository;
    @Autowired
    private IFilesRepository filesRepository;
    @Autowired
    private IQuestionRepository questionRepository;


    @Override
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public List<UserBean> getUsers(@RequestParam(defaultValue = "0") int page,
                                   @RequestParam(defaultValue = "20") int count) {
        // todo: pagination
        List<User> users = StreamSupport.stream(userRepository.findAll(new PageRequest(page, count)).spliterator(), false)
                .collect(Collectors.toList());
        List<UserBean> result = new ArrayList<>();
        for (User user : users) {
            result.add(castUser(user));
        }
        return result;
    }

    @Override
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public User getUserById(@PathVariable Long id) {
        return userRepository.findOne(id);
    }

    @Override
    @RequestMapping(value = "/data/{id}", method = RequestMethod.GET)
    public SimpleResponse getUserByIdSimple(@PathVariable Long id) {
        User user = userRepository.findOne(id);
        if (user == null) {
            return new SimpleResponse("no user with such id").ERROR_NOT_FOUND();
        }
        UserBean userBean = castUser(user);
        return new SimpleResponse(userBean).SUCCESS();
    }

    @Override
    @RequestMapping(value = "/registration", method = RequestMethod.POST)
    public User regUser(@RequestBody @Valid User user) {
        userProcessor.mergeUser(user);
        return userRepository.save(user);
    }

    @Override
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public SimpleResponse checkUser(@RequestBody @Valid User user) {
        User localUser = userRepository.findByIin(user.getIin());
        System.out.println("user" + user.toString());
        if (localUser == null) {
            return new SimpleResponse("no user with such userName").ERROR_NOT_FOUND();
        }
        if (localUser.getPassword().equals(user.getPassword())) {
            UserBean userBean = castUser(localUser);
            return new SimpleResponse(userBean).SUCCESS();
        } else {
            return new SimpleResponse("Неверный пароль").ERROR();
        }
    }

    @Override
    @RequestMapping(value = "/update", method = RequestMethod.POST)
    public SimpleResponse updateUser(@RequestBody @Valid User user) {
        User localUser = userRepository.findOne(user.getId());
        if (localUser == null) {
            return new SimpleResponse("no user with such id").ERROR_NOT_FOUND();
        } else {
            localUser.setPassword(pwd(user.getPassword()));
            localUser = userRepository.save(localUser);
            return new SimpleResponse(localUser).SUCCESS();
        }
    }

    @Override
    @RequestMapping(value = "/delete", method = RequestMethod.DELETE)
    public SimpleResponse deleteUser(@RequestBody @Valid User user) {
        User localUser = userRepository.findOne(user.getId());
        if (localUser == null) {
            return new SimpleResponse("no user with such id").ERROR_NOT_FOUND();
        } else {
            userRepository.delete(localUser);
            return new SimpleResponse("user deleted").SUCCESS();
        }
    }

    @Override
    @RequestMapping(value = "/orgs/{userId}", method = RequestMethod.GET)
    public List<Organisation> getAllOrgs(@PathVariable Long userId) {
        User localUser = userRepository.findOne(userId);
        List<Organisation> result = new ArrayList<>();
        for (UserRoles userRoles : localUser.getUserRolesSet()) {
            result.add(userRoles.getOrgId());
        }
        return result;
    }

    @Override
    @RequestMapping(value = "/orgs/workvoting/{userId}", method = RequestMethod.GET)
    public List<OrgBean> getAllOrgsWithWorkVoting(@PathVariable Long userId) {
        User localUser = userRepository.findOne(userId);
        List<OrgBean> result = new ArrayList<>();
        if (localUser != null) {
            for (UserRoles userRoles : localUser.getUserRolesSet()) {
                OrgBean organisation = new OrgBean();
                organisation.setId(userRoles.getOrgId().getId());
                organisation.setExternalId(userRoles.getOrgId().getExternalId());
                organisation.setOrganisationName(userRoles.getOrgId().getOrganisationName());
                organisation.setOrganisationNum(userRoles.getOrgId().getOrganisationNum());
                organisation.setStatus(userRoles.getOrgId().getStatus());
                organisation.setShareCount(userRoles.getShareCount() == null ? 0 : userRoles.getShareCount());
                List<VotingBean> vSet = new ArrayList<>();
                boolean canAdd = false;
                for (Voting voting : userRoles.getOrgId().getVotingSet()) {
                    if (voting.getDateClose() == null) {
                        VotingBean votingBean = castToBean(voting, localUser);
                        vSet.add(votingBean);
                        canAdd = true;
                    }
                }
                organisation.setVotingSet(vSet);
                if (canAdd) {
                    result.add(organisation);
                }
            }
        }
        return result;
    }

    @Override
    @RequestMapping(value = "/orgs/oldvoting/{userId}", method = RequestMethod.GET)
    public List<OrgBean> getAllOrgsWithOldVoting(@PathVariable Long userId) {
        User localUser = userRepository.findOne(userId);
        List<OrgBean> result = new ArrayList<>();
        for (UserRoles userRoles : localUser.getUserRolesSet()) {
            OrgBean organisation = new OrgBean();
            organisation.setId(userRoles.getOrgId().getId());
            organisation.setExternalId(userRoles.getOrgId().getExternalId());
            organisation.setOrganisationName(userRoles.getOrgId().getOrganisationName());
            organisation.setOrganisationNum(userRoles.getOrgId().getOrganisationNum());
            organisation.setStatus(userRoles.getOrgId().getStatus());
            organisation.setShareCount(userRoles.getShareCount() == null ? 0 : userRoles.getShareCount());
            List<VotingBean> vSet = new ArrayList<>();
            boolean canAdd = false;
            for (Voting voting : userRoles.getOrgId().getVotingSet()) {
                if (voting.getDateClose() != null) {
                    VotingBean votingBean = castToBean(voting, localUser);
                    vSet.add(votingBean);
                    canAdd = true;
                }
            }
            organisation.setVotingSet(vSet);
            if (canAdd) {
                result.add(organisation);
            }
        }
        return result;
    }

    @Override
    @RequestMapping(value = "/complate/{votingId}/{userId}", method = RequestMethod.POST)
    public SimpleResponse complateVoting(@PathVariable Long votingId, @PathVariable Long userId, @RequestBody @Valid ConfirmBean bean) {
        if (confirmationService.check(bean)) {
            Voting voting = votingRepository.findOne(votingId);
            User user = userRepository.findOne(userId);
            Voter voter = voterRepository.findByVotingIdAndUserId(voting, user);
            if (voter != null) {
                voter.setDateVoting(bean.getDateConfirm());
                voter.setPublicKey(bean.getPublicKey());
                voter.setSignature(bean.getSignature());
                voter = voterRepository.save(voter);
                return new SimpleResponse(voter).SUCCESS();
            } else {
                return new SimpleResponse("Не верные данные голосующего").ERROR_CUSTOM();
            }
        } else {
            return new SimpleResponse("Данные не прошли проверку").ERROR_CUSTOM();
        }
    }

    @Override
    @RequestMapping(value = "/questions/{votingId}/{userId}", method = RequestMethod.GET)
    public List<QuestionBean> getVotingQuestions(@PathVariable Long votingId, @PathVariable Long userId) {
        Voting voting = votingRepository.findOne(votingId);
        User user = userRepository.findOne(userId);
        List<QuestionBean> result = new ArrayList<>();

        if (voting != null && user != null && canVote(voting, user)) {
            List<Question> question = questionRepository.findByVotingId(voting);
            for (Question q : question) {
                QuestionBean bean = castFromQuestion(q);
                result.add(bean);
            }

        }
        return result;
    }

    //функция для криптовки паролей
    public static String pwd(String password) {
        return password;
    }

    //функция создания UserBean из User
    public UserBean castUser(User user) {

        UserBean userBean = new UserBean();
        userBean.setId(user.getId());
        userBean.setLogin(user.getUsername());
        userBean.setIin(user.getIin());
        if (user.getUserInfoId() != null) {
            userBean.setUserInfo(user.getUserInfoId());
        }
        if (!user.getUserRolesSet().isEmpty()) {
            Role role = Role.ROLE_USER;
            for (UserRoles userRole : user.getUserRolesSet()) {
                Role temp = userRole.getRole();
                if (role.compareTo(temp) > 0) {
                    role = temp;
                }
            }
            userBean.setRole(role);
        }
        return userBean;
    }

    @Override
    public QuestionBean castFromQuestion(Question q) {
        QuestionBean result = new QuestionBean();
        result.setId(q.getId());
        result.setDecision(q.getDecision());
        result.setNum(q.getNum());
        result.setQuestion(q.getQuestion());
        result.setQuestionType(q.getQuestionType());
        result.setVotingId(q.getVotingId().getId());
        result.setAnswerSet(q.getAnswerSet());
        Set<DecisionBean> beanSet = new HashSet();
        for (Decision decision : q.getDecisionSet()) {
            DecisionBean bean = getBeanFromDecision(decision);
            beanSet.add(bean);
        }
        result.setDecisionSet(beanSet);
        for (Answer answer : q.getAnswerSet()) {

        }
        return result;
    }

    @Override
    public boolean canVote(Voting voting, User user) {
        boolean result = false;
        for (Voter voter : voting.getVoterSet()) {
            if (voter.getUserId().equals(user)) {
                result = true;
                break;
            }
        }
        return result;
    }

    @Override
    public VoterBean castToBean(Voting voting, Voter voter) {
        VoterBean result = new VoterBean();
        result.setId(voter.getId());
        result.setShareCount(voter.getShareCount());
        Set<DecisionBean> beanSet = new HashSet();
        for (Decision decision : voter.getDecisionSet()) {
            if (voter.getVotingId().equals(voting)) {
                DecisionBean bean = getBeanFromDecision(decision);
                beanSet.add(bean);
            }
        }
        result.setDecisions(beanSet);
//        result.setVoting(castToBean(voter.getVotingId(),voter.getUserId()));
        result.setUserId(castUser(voter.getUserId()));
        result.setSharePercent((float) 100 * voter.getShareCount() / getVotingAllScore(voting.getId()));
        return result;
    }

    @Override
    public VotingBean castToBean(Voting voting, User user) {
        VotingBean result = new VotingBean();
        result.setCanVote(canVote(voting, user));
        result.setDateBegin(voting.getDateBegin());
        result.setDateClose(voting.getDateClose());
        result.setDateCreate(voting.getDateCreate());
        result.setDateEnd(voting.getDateEnd());
        result.setId(voting.getId());
        result.setLastChanged(voting.getLastChanged());

        if (user.getId().equals(0L)) {
            Set<QuestionBean> questionBeanSet = new HashSet<>();
            for (Question question : voting.getQuestionSet()) {
                questionBeanSet.add(castFromQuestion(question));
            }
            result.setQuestionSet(questionBeanSet);
        }
        result.setStatus(voting.getStatus());
        result.setSubject(voting.getSubject());
        result.setVotingType(voting.getVotingType());
        //TODO добавить проверку прав
        if (user.getId().equals(0L)) {
            Set<VoterBean> voterBeanSet = new HashSet<>();
            for (Voter voter : voting.getVoterSet()) {
                if (voter.getUserId().equals(user)) {
                    voterBeanSet.add(castToBean(voting, voter));
                }
            }
            result.setVoterSet(voterBeanSet);
        }
        return result;
    }

    public DecisionBean getBeanFromDecision(Decision decision) {
        DecisionBean result = new DecisionBean();
        result.setId(decision.getId());
        result.setScore(decision.getScore());
        result.setAnswerId(decision.getAnswerId() == null ? null : decision.getAnswerId().getId());
        result.setComments(decision.getComments());
        result.setDateCreate(decision.getDateCreate());
        result.setQuestionId(decision.getQuestionId().getId());
        result.setUserId(decision.getVoterId().getUserId().getId());
        return result;
    }

    public Integer getVotingAllScore(Long votingId) {
        Integer result = 0;
        Voting voting = votingRepository.findOne(votingId);
        for (Voter voter : voting.getVoterSet()) {
            result = result + voter.getShareCount();
        }
        return result;
    }

    @Override
    @RequestMapping(value = "/questionfiles/{votingId}/{questionId}", method = RequestMethod.GET)
    public List<Files> getVotingQuestionFiles(@PathVariable Long votingId, @PathVariable Long questionId) {
        Voting voting = votingRepository.findOne(votingId);
        List<Files> files = filesRepository.findByVotingId(voting);
        List<Files> result = new ArrayList<>();
        for (Files file:files) {
            if (!file.getQuestionFileSet().isEmpty()) {
                for (QuestionFile questionFile:file.getQuestionFileSet()) {
                    if (questionFile.getQuestionId().getId().equals(questionId)) {
                        result.add(file);
                    }
                }
            }
        }
        return result;
    }

    @Override
    @RequestMapping(value = "/questionfile/{filePath}", method = RequestMethod.GET)
    public void getVotingQuestions(@PathVariable String filePath,
                                   HttpServletResponse response) {
        File file = new File("/opt/voting/files/"+filePath+".pdf");
        try {
            // get your file as InputStream
            InputStream is = new FileInputStream("/opt/voting/files/"+filePath+".pdf");
            // copy it to response's OutputStream
            org.apache.commons.io.IOUtils.copy(is, response.getOutputStream());
            response.flushBuffer();
        } catch (IOException ex) {
            throw new RuntimeException("IOError writing file to output stream");
        }
    }
}
