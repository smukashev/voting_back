package kz.bsbnb.controller.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import kz.bsbnb.block.bean.AccAnswer;
import kz.bsbnb.block.bean.AccQuestion;
import kz.bsbnb.block.controller.voting.IVotingInvoke;
import kz.bsbnb.block.controller.voting.IVotingQuery;
import kz.bsbnb.block.model.HLMessage;
import kz.bsbnb.block.util.BlockChainProperties;
import kz.bsbnb.common.bean.*;
import kz.bsbnb.common.consts.Locale;
import kz.bsbnb.common.consts.QuestionType;
import kz.bsbnb.common.consts.Role;
import kz.bsbnb.common.consts.VotingType;
import kz.bsbnb.common.external.ReestrHead;
import kz.bsbnb.common.model.*;
import kz.bsbnb.common.util.*;
import kz.bsbnb.controller.IUserController;
import kz.bsbnb.controller.IVotingController;
import kz.bsbnb.processor.AttributeProcessor;
import kz.bsbnb.processor.VoterProcessor;
import kz.bsbnb.processor.VotingProcessor;
import kz.bsbnb.repository.*;
import kz.bsbnb.security.ConfirmationService;
import kz.bsbnb.util.*;
import kz.bsbnb.util.processor.MessageProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Created by Rusaln on 20.10.2016
 */
@RestController
@RequestMapping(value = "/voting")
public class VotingControllerImpl implements IVotingController {

    @Autowired
    IVotingRepository votingRepository;

    @Autowired
    IUserRepository userRepository;

    @Autowired
    IUserRoleRepository userRoleRepository;

    @Autowired
    IQuestionRepository questionRepository;

    @Autowired
    IAnswerRepository answerRepository;

    @Autowired
    IVoterRepository voterRepository;

    @Autowired
    IDecisionRepository decisionRepository;

    @Autowired
    IUserController userController;

    @Autowired
    IOrganisationRepository organisationRepository;

    @Autowired
    IFilesRepository filesRepository;

    @Autowired
    IQuestionFileRepository questionFileRepository;

    @Autowired
    AttributeProcessor attributeProcessor;

    @Autowired
    IAttributeRepository attributeRepository;
    @Autowired
    ConfirmationService confirmationService;

    @Autowired
    private BlockChainProperties blockchainProperties;

    @Autowired
    IVotingQuery votingQuery;

    @Autowired
    IVotingInvoke votingInvoke;

    @Autowired
    IReestrHeadRepository reestrHeadRepository;

    @Autowired
    MessageProcessor messageProcessor;

    @Autowired
    private VoterProcessor voterProcessor;

    @Autowired
    private VotingProcessor votingProcessor;



    @RequestMapping(value = "/save", method = RequestMethod.POST)
    public SimpleResponse save(@RequestBody @Valid Voting voting) {
        return votingProcessor.saveVoting(voting);
    }

    @Override
    @RequestMapping(value = "/list/{userId}", method = RequestMethod.GET)
    public List<Voting> getVotings(@PathVariable Long userId,
                                   @RequestParam(defaultValue = "0") int page,
                                   @RequestParam(defaultValue = "20") int count) {
/*        User user = userRepository.findOne(userId);
        return StreamSupport.stream(votingRepository.findWorkingForUser(user).spliterator(), false)
                .collect(Collectors.toList());
                */
        User user = userRepository.findOne(userId);
        List<Voting> list = votingRepository.findWorkingForUser(user);
        for (Voting v : list) {
            System.out.println(v.getId());
        }
        return list;
    }

    @Override
    @RequestMapping(value = "/list/old/{userId}", method = RequestMethod.GET)
    public List<Voting> getOldVotingsForUser(@PathVariable Long userId,
                                             @RequestParam(defaultValue = "0") int page,
                                             @RequestParam(defaultValue = "20") int count) {
/*        User user = userRepository.findOne(userId);
        return StreamSupport.stream(votingRepository.findWorkingForUser(user).spliterator(), false)
                .collect(Collectors.toList());
                */
        User user = userRepository.findOne(userId);
        List<Voting> list = votingRepository.findOldForUser(user);
        for (Voting v : list) {
            System.out.println(v.getId());
        }
        return list;
    }

    @Override
    @RequestMapping(value = "/new", method = RequestMethod.POST)
    public SimpleResponse createVoting(@RequestBody @Valid RegVotingBean votingBean) {
        User user = userRepository.findOne(votingBean.getUserId());
        Voting voting = castFromBean(votingBean, user);
        if (voting.getDateBegin() != null) {
            if (voting.getDateEnd() != null) {
                if (voting.getDateEnd().after(voting.getDateBegin())) {
                    voting = votingRepository.save(voting);
                    RegVotingBean result = castToBean(voting);
                    return new SimpleResponse(result).SUCCESS();
                } else {
                    return new SimpleResponse("Дата окончания голосования должна быть позже даты начала голосования").ERROR_CUSTOM();
                }
            } else {
                return new SimpleResponse("Дата окончания голосования не может быть пустым").ERROR_CUSTOM();
            }
        } else {
            return new SimpleResponse("Дата начала голосования не может быть пустым").ERROR_CUSTOM();
        }
    }

    @Override
    @RequestMapping(value = "/edit", method = RequestMethod.POST)
    public SimpleResponse editVoting(@RequestBody @Valid RegVotingBean votingBean) {
        //TODO Проверка полей
        User user = userRepository.findOne(votingBean.getUserId());
        Voting voting = castFromBean(votingBean, user);
        Voting oldVoting = votingRepository.findOne(votingBean.getId());
        if (oldVoting != null) {
            oldVoting.setWhoChanged(user);
            oldVoting.setLastChanged(new Date());
            oldVoting.setDateEnd(voting.getDateEnd());
            oldVoting.setDateBegin(voting.getDateBegin());
            oldVoting.setMessages(voting.getMessages());
            oldVoting.setVotingType(VotingType.valueOf(voting.getVotingType()));
            oldVoting = votingRepository.save(oldVoting);
            RegVotingBean result = castToBean(oldVoting);
            return new SimpleResponse(result).SUCCESS();
        } else {
            return new SimpleResponse("Не найдено голосование с ID=" + votingBean.getId()).ERROR_NOT_FOUND();
        }
    }

    @Override
    @RequestMapping(value = "/editWhenStarted", method = RequestMethod.POST)
    public SimpleResponse editVotingWhenStarted(@RequestBody @Valid RegVotingBean votingBean, @RequestParam(defaultValue = "0") String reason) {
        if (reason.equals("0") || "".equals(reason)) {
            return new SimpleResponse("Укажите причину").ERROR_CUSTOM();
        } else {
            User user = userRepository.findOne(votingBean.getUserId());
            Voting voting = castFromBean(votingBean, user);
            Voting oldVoting = votingRepository.findOne(votingBean.getId());
            if (oldVoting != null && oldVoting.getStatus().equals("STARTED")) {
                List<Attribute> attributes = new ArrayList<>();
                oldVoting.setWhoChanged(user);
                oldVoting.setLastChanged(new Date());
                if (!voting.getDateEnd().equals(oldVoting.getDateEnd())) {
                    Attribute a = new Attribute();
                    a.setValue(oldVoting.getDateEnd().toString());
                    a.setObject("VOTE");
                    a.setObjectId(oldVoting.getId());
                    a.setTypeValue("DATE_END");
                    attributes.add(a);
                }
                oldVoting.setDateEnd(voting.getDateEnd());
                if (!voting.getDateBegin().equals(oldVoting.getDateBegin())) {
                    Attribute a = new Attribute();
                    a.setValue(oldVoting.getDateBegin().toString());
                    a.setObject("VOTE");
                    a.setObjectId(oldVoting.getId());
                    a.setTypeValue("DATE_BEGIN");
                    attributes.add(a);
                }
                oldVoting.setDateBegin(voting.getDateBegin());
                if (!attributes.isEmpty()) {
                    Attribute a = new Attribute();
                    a.setValue(reason);
                    a.setObject("VOTE");
                    a.setObjectId(oldVoting.getId());
                    a.setTypeValue("REASON");
                    attributes.add(a);
                    attributeProcessor.merge("VOTE", oldVoting.getId(), attributes);
                }

                oldVoting = votingRepository.save(oldVoting);
                RegVotingBean result = castToBean(oldVoting);
                return new SimpleResponse(result).SUCCESS();
            } else {
                return new SimpleResponse("Не найдено действующее голосование с ID=" + votingBean.getId()).ERROR_NOT_FOUND();
            }
        }
    }

    private RegVotingBean castToBean(Voting voting) {
        RegVotingBean result = new RegVotingBean();
        result.setDateBegin(voting.getDateBegin());
        result.setDateCreate(voting.getDateCreate());
        result.setDateEnd(voting.getDateEnd());
        result.setId(voting.getId());
        result.setOrganisationId(voting.getOrganisation().getId());
        result.setMessages(voting.getMessages());
        result.setVotingType(voting.getVotingType());
        return result;
    }

    private Voting castFromBean(RegVotingBean votingBean, User user) {
        Voting result = new Voting();
        result.setDateBegin(votingBean.getDateBegin());
        result.setVotingType(VotingType.getEnum(votingBean.getVotingType()));
        result.setMessages(votingBean.getMessages());
        result.setDateEnd(votingBean.getDateEnd());
        result.setDateCreate(votingBean.getDateCreate() == null ? new Date() : votingBean.getDateCreate());
        result.setStatus("NEW");
        result.setWhoChanged(user);
        result.setLastChanged(new Date());
        Organisation org = organisationRepository.findOne(votingBean.getOrganisationId());
        result.setOrganisation(org);
        return result;
    }

    @Override
    @RequestMapping(value = "/work/{userId}", method = RequestMethod.GET)
    public List<Voting> getWorkVotings(@PathVariable Long userId,
                                       @RequestParam(defaultValue = "0") int page,
                                       @RequestParam(defaultValue = "20") int count) {
        User user = userRepository.findOne(userId);
        List<Voting> voting = StreamSupport.stream(votingRepository.findByUser(user, new PageRequest(page, count)).spliterator(), false)
                .collect(Collectors.toList());
        List<Voting> result = new ArrayList<>();
        for (Voting next : voting) {
            if (next.getDateEnd() == null) {
                result.add(next);
            }
        }
        return result;
    }

    @Override
    @RequestMapping(value = "/old/{userId}", method = RequestMethod.GET)
    public List<Voting> getOldVotings(@PathVariable Long userId,
                                      @RequestParam(defaultValue = "0") int page,
                                      @RequestParam(defaultValue = "20") int count) {
        User user = userRepository.findOne(userId);
        List<Voting> voting = StreamSupport.stream(votingRepository.findByUser(user, new PageRequest(page, count)).spliterator(), false)
                .collect(Collectors.toList());
        List<Voting> result = new ArrayList<>();
        for (Voting next : voting) {
            if (next.getDateEnd() != null) {
                result.add(next);
            }
        }
        return result;
    }

    @Override
    @RequestMapping(value = "/allq/{votingId}", method = RequestMethod.GET)
    public List<QuestionBean> getVotingQuestions(@PathVariable Long votingId,
                                                 @RequestParam(defaultValue = "0") int page,
                                                 @RequestParam(defaultValue = "20") int count) {
        Voting voting = votingRepository.findOne(votingId);
        if (voting == null) {
            return new ArrayList<>();
        } else {
            List<Question> question = questionRepository.findByVoting(voting);
            List<QuestionBean> result = new ArrayList<>();
            for (Question q : question) {
                QuestionBean bean = userController.castFromQuestion(q, new User(), false);
                result.add(bean);
            }
            return result;
        }
    }

    @Override
    @RequestMapping(value = "/{votingId}", method = RequestMethod.GET)
    public SimpleResponse getVoting(@PathVariable Long votingId) {
        Voting voting = votingRepository.findOne(votingId);
        voting.setHasQuestions(!voting.getQuestionSet().isEmpty());
        if (voting == null) {
            return new SimpleResponse("Не найдено голосование с ID=" + votingId).ERROR_NOT_FOUND();
        } else {
            return new SimpleResponse(voting).SUCCESS();
        }
    }


    @Override
    @RequestMapping(value = "/q/{votingId}/{qid}", method = RequestMethod.GET)
    public QuestionBean getVotingQuestion(@PathVariable Long votingId, @PathVariable Long qid) {
        Voting voting = votingRepository.findOne(votingId);
        List<Question> question = questionRepository.findByVoting(voting);
        QuestionBean result = new QuestionBean();
        for (Question q : question) {
            if (q.getId().equals(qid)) {
                result = userController.castFromQuestion(q, new User(), true);
                break;
            }
        }
        return result;
    }

    @Override
    @RequestMapping(value = "/questions/{votingId}/{userId}", method = RequestMethod.GET)
    public List<QuestionBean> getVotingQuestions(@PathVariable Long votingId, @PathVariable Long userId) {
        Voting voting = votingRepository.findOne(votingId);
        User user = userRepository.findOne(userId);
        List<QuestionBean> result = new ArrayList<>();
        if (voting != null && user != null) {
            List<Question> question = questionRepository.findByVoting(voting);
            boolean notUser = !userController.getRole(user, voting.getOrganisation()).equals(Role.ROLE_USER);
            for (Question q : question) {
                QuestionBean bean = userController.castFromQuestion(q, user, notUser);
                result.add(bean);
            }

        }
        return result;
    }

    @Override
    @RequestMapping(value = "/start/{votingId}/{userId}", method = RequestMethod.POST)
    public SimpleResponse startVoting(@PathVariable Long votingId, @PathVariable Long userId
            , @RequestBody @Valid ConfirmBean confirmBean) {
        if (confirmationService.check(confirmBean)) {
            Voting voting = votingRepository.findOne(votingId);
            User user = userRepository.findOne(userId);
//            voting.setStatus("STARTED");
            voting.setDateBegin(new Date());
            voting.setStatus("TO_BLOCKCHAIN");
            voting = votingRepository.save(voting);
            return new SimpleResponse(userController.castToBean(voting, user));
        } else {
            return new SimpleResponse("Данные не прошли проверку").ERROR_CUSTOM();
        }
    }

    @Override
    @RequestMapping(value = "/delete/{votingId}/{userId}", method = RequestMethod.DELETE)
    public SimpleResponse deleteVoting(@PathVariable Long votingId, @PathVariable Long userId
            , @RequestBody @Valid ConfirmBean confirmBean) {
        if (confirmationService.check(confirmBean)) {
            Voting voting = votingRepository.findOne(votingId);
            if (voting == null) {
                return new SimpleResponse("Не найдено голосование с ID=" + votingId).ERROR_NOT_FOUND();
            } else {
                User user = userRepository.findOne(userId);
                if (voting.getStatus().equals("CREATED") || voting.getStatus().equals("NEW")) {
                    for (Question question : voting.getQuestionSet()) {
                        deleteVotingQuestions(votingId, question);
                    }
                    votingRepository.delete(voting);
                    return new SimpleResponse("Голосование успешно удалено").SUCCESS();
                } else {
                    return new SimpleResponse("Голосование не может быть удалено").ERROR_CUSTOM();
                }
            }
        } else {
            return new SimpleResponse("Данные не прошли проверку").ERROR_CUSTOM();
        }
    }

    @Override
    @RequestMapping(value = "/restart/{votingId}/{userId}", method = RequestMethod.POST)
    public SimpleResponse restartVoting(@PathVariable Long votingId, @PathVariable Long userId
            , @RequestBody @Valid ConfirmBean confirmBean) {
        if (confirmationService.check(confirmBean)) {
            Voting voting = votingRepository.findOne(votingId);
            if (voting == null) {
                return new SimpleResponse("Не найдено голосование с ID=" + votingId).ERROR_NOT_FOUND();
            } else {
                User user = userRepository.findOne(userId);
//                voting.setStatus("STARTED");
                voting.setDateBegin(new Date());
                voting.setStatus("STARTED");
                voting.setDateClose(null);
                voting = votingRepository.save(voting);
                return new SimpleResponse(userController.castToBean(voting, user));
            }
        } else {
            return new SimpleResponse("Данные не прошли проверку").ERROR_CUSTOM();
        }
    }

    @Override
    @RequestMapping(value = "/stop/{votingId}/{userId}", method = RequestMethod.POST)
    public SimpleResponse stopVoting(@PathVariable Long votingId, @PathVariable Long userId
            , @RequestBody @Valid ConfirmBean confirmBean) {
        if (confirmationService.check(confirmBean)) {
            Voting voting = votingRepository.findOne(votingId);
            if (voting == null) {
                return new SimpleResponse("Не найдено голосование с ID=" + votingId).ERROR_NOT_FOUND();
            } else {
                User user = userRepository.findOne(userId);
                voting.setStatus("STOPED");
                voting.setDateClose(new Date());
                voting.setKvoroom(calcKvoroom(voting));
                voting.setStatus("CLOSED");
                voting = votingRepository.save(voting);
                return new SimpleResponse(userController.castToBean(voting, user));
            }
        } else {
            return new SimpleResponse("Данные не прошли проверку").ERROR_CUSTOM();
        }
    }

    @Override
    @RequestMapping(value = "/close/{votingId}/{userId}", method = RequestMethod.POST)
    public SimpleResponse closeVoting(@PathVariable Long votingId, @PathVariable Long userId
            , @RequestBody @Valid ConfirmBean confirmBean) {
        if (confirmationService.check(confirmBean)) {
            Voting voting = votingRepository.findOne(votingId);
            if (voting == null) {
                return new SimpleResponse("Не найдено голосование с ID=" + votingId).ERROR_NOT_FOUND();
            } else {
                User user = userRepository.findOne(userId);
                voting.setDateClose(new Date());
                voting.setKvoroom(calcKvoroom(voting));
                voting.setStatus("CLOSED");
                voting = votingRepository.save(voting);
                return new SimpleResponse(userController.castToBean(voting, user));
            }
        } else {
            return new SimpleResponse("Данные не прошли проверку").ERROR_CUSTOM();
        }
    }


    @Override
    @RequestMapping(value = "/addq/{votingId}", method = RequestMethod.POST)
    public SimpleResponse addVotingQuestions(@PathVariable Long votingId, @RequestBody @Valid RegQuestionBean question) {
        Voting voting = votingRepository.findOne(votingId);
        if (voting == null) {
            return new SimpleResponse("Голосование с id (" + votingId + ") не найдено").ERROR_NOT_FOUND();
        } else if (voting.getStatus().equals("NEW") || voting.getStatus().equals("CREATED")) {

            Question result = new Question();
            result.setMessages(question.getMessages());
            result.setQuestionType(question.getQuestionType());
            result.setMaxCount(question.getMaxCount() == null ? 1 : question.getMaxCount());
            result.setVoting(voting);
            result.setPrivCanVote(question.getPrivCanVote());
            //result.setNum(questionRepository.getMaxNumForQuestion(votingId) + 1);
            List<Question> list = questionRepository.findByVoting(voting);
            int max = 0;
            try {
                for (Question qq : list) {
                    if (max < qq.getNum()) max = qq.getNum();
                }
            } catch (Exception e) {
                max = 0;
            }
            result.setNum(max + 1);
            result = questionRepository.save(result);
            try {
                for (Long fileId : question.getFilesId()) {
                    Files file = filesRepository.findOne(fileId);
                    if (file != null) {
                        QuestionFile questionFile = new QuestionFile();
                        questionFile.setQuestion(result);
                        questionFile.setFiles(file);
                        questionFileRepository.save(questionFile);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (question.getQuestionType().equals(QuestionType.ORDINARY.name())) {
                addOrdinaryAnswer(result);
            }
            return new SimpleResponse(result).SUCCESS();
        } else {
            return new SimpleResponse("Голосование в ненадлежайшем статусе").ERROR_CUSTOM();
        }
    }

    @Override
    @RequestMapping(value = "/editq/{votingId}", method = RequestMethod.POST)
    public SimpleResponse editVotingQuestions(@PathVariable Long votingId, @RequestBody @Valid RegQuestionBean question) {
        Voting voting = votingRepository.findOne(votingId);
        if (voting == null) {
            return new SimpleResponse("Голосование с id (" + votingId + ") не найдено").ERROR_NOT_FOUND();
        } else if (voting.getStatus().equals("NEW") || voting.getStatus().equals("CREATED")) {
            Question ques = questionRepository.findOne(question.getId());
            ques.setMessages(question.getMessages());
            ques.setMaxCount(question.getMaxCount() == null ? ques.getMaxCount() : question.getMaxCount());
            ques.setVoting(voting);
            ques = questionRepository.save(ques);
            for (QuestionFile file : ques.getQuestionFileSet()) {
                questionFileRepository.deleteByIds(file.getId());
            }
            for (Long fileId : question.getFilesId()) {
                Files file = filesRepository.findOne(fileId);
                if (file != null) {
                    QuestionFile questionFile = new QuestionFile();
                    questionFile.setQuestion(ques);
                    questionFile.setFiles(file);
                    questionFileRepository.save(questionFile);
                }
            }
            return new SimpleResponse(ques).SUCCESS();
        } else {
            return new SimpleResponse("Голосование в ненадлежайшем статусе").ERROR_CUSTOM();
        }
    }

    @Override
    @RequestMapping(value = "/delq/{votingId}", method = RequestMethod.DELETE)
    public SimpleResponse deleteVotingQuestions(@PathVariable Long votingId, @RequestBody @Valid Question question) {
        Voting voting = votingRepository.findOne(votingId);
        //TODO Добавить проверку статуса голосования
        if (voting == null) {
            return new SimpleResponse("Голосование с id (" + votingId + ") не найдено").ERROR_NOT_FOUND();
        } else {
            Question ques = questionRepository.findOne(question.getId());
            if (ques != null && ques.getVoting().equals(voting)) {
                if (ques.getVoting().getDateBegin().after(new Date())) {
                    deleteVotingAnswers(question);
                    questionRepository.deleteByIds(ques.getId());
                } else {
                    return new SimpleResponse("По данному вопросу уже есть решения. Удаление не возможно").ERROR_CUSTOM();
                }
                //TODO Добавить обращение у HL
                return new SimpleResponse("Вопрос удален").SUCCESS();
            } else {
                return new SimpleResponse("Вопрос не найден").SUCCESS();
            }
        }
    }

    private void deleteVotingAnswers(Question question) {
        List<Answer> answers = answerRepository.findByQuestionId(question);
        for (Answer answer : answers) {
            answerRepository.deleteByIds(answer.getId());
        }
    }

    @Override
    @RequestMapping(value = "/addq/answer/{questionId}", method = RequestMethod.POST)
    public SimpleResponse addVotingAnswer(@PathVariable Long questionId, @RequestBody @Valid Answer answer) {
        Question question = questionRepository.findOne(questionId);
        //TODO Добавить проверку статуса голосования
        if (question == null) {
            return new SimpleResponse("question with id (" + questionId + ") not found").ERROR_NOT_FOUND();
        } else {
            Answer result = new Answer();
            result.setMessages(answer.getMessages());
            result.setQuestion(question);
            result = answerRepository.save(result);
            //TODO Добавить обращение у HL
            return new SimpleResponse(result).SUCCESS();
        }
    }

    @Override
    @RequestMapping(value = "/editq/answer/{questionId}", method = RequestMethod.POST)
    public SimpleResponse editVotingAnswer(@PathVariable Long questionId, @RequestBody @Valid Answer answer) {
        Question question = questionRepository.findOne(questionId);
        //TODO Добавить проверку статуса голосования
        if (question == null) {
            return new SimpleResponse("question with id (" + questionId + ") not found").ERROR_NOT_FOUND();
        } else {
            Answer result = answerRepository.findOne(answer.getId());
            result.setMessages(answer.getMessages());
            result = answerRepository.save(result);
            //TODO Добавить обращение у HL
            return new SimpleResponse(result).SUCCESS();
        }
    }

    @Override
    @RequestMapping(value = "/delq/answer/{questionId}", method = RequestMethod.DELETE)
    public SimpleResponse deleteVotingAnswer(@PathVariable Long questionId, @RequestBody @Valid Answer answer) {
        Question question = questionRepository.findOne(questionId);
        //TODO Добавить проверку статуса голосования
        if (question == null) {
            return new SimpleResponse("question with id (" + questionId + ") not found").ERROR_NOT_FOUND();
        } else {
            Answer result = answerRepository.findOne(answer.getId());
            answerRepository.delete(result);
            //TODO Добавить обращение у HL
            return new SimpleResponse("Вопрос удален").SUCCESS();
        }
    }


    @Override
    @RequestMapping(value = "/delVoter/{votingId}/{userId}", method = RequestMethod.DELETE)
    public SimpleResponse delVoter(@PathVariable Long votingId, @PathVariable Long userId) {
        Voting voting = votingRepository.findOne(votingId);
        User user = userRepository.findOne(userId);
        Voter voter = voterRepository.findByVotingIdAndUserId(voting, user);
        if (voter != null) {
            voterRepository.delete(voter);
            return new SimpleResponse("Голосующий удален").SUCCESS();
        } else {
            return new SimpleResponse("Голосующего не существует").ERROR_CUSTOM();
        }
    }


    @Override
    @RequestMapping(value = "/addVoter/{userId}", method = RequestMethod.POST)
    public SimpleResponse addVoter(@PathVariable Long userId, @RequestBody @Valid RegVoterBean regVoterBean) {
         return new SimpleResponse("Добавить голосующего может только администратор").ERROR_CUSTOM();
    }


    private void addListAnswer(Question question, List<Answer> answers) {
        for (Answer next : answers) {
            Answer result = new Answer();
            result.setMessages(next.getMessages());
            result.setQuestion(question);
            //TODO Добавить обращение у HL
            answerRepository.save(result);
        }
    }

    private AnswerMessage getOrdinaryAnswerMessage(Answer answer, String message, Locale locale) {
        return new AnswerMessage(answer, locale, message);
    }

    private void addOrdinaryAnswer(Question question) {
        List<Answer> answers = new ArrayList<>();
        Answer answerYes = new Answer();
        answerYes.setQuestion(question);
        answerYes.getMessages().add(getOrdinaryAnswerMessage(answerYes, "За", Locale.ru));
        answers.add(answerYes);
        Answer answerNo = new Answer();
        answerNo.setQuestion(question);
        answerNo.getMessages().add(getOrdinaryAnswerMessage(answerNo, "Против", Locale.ru));
        answers.add(answerNo);
        Answer answerAbstained = new Answer();
        answerAbstained.setQuestion(question);
        answerAbstained.getMessages().add(getOrdinaryAnswerMessage(answerAbstained, "Воздержался", Locale.ru));
        answers.add(answerAbstained);
        addListAnswer(question, answers);
    }

    @Override
    public Decision getDecisionFromBean(DecisionBean bean) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat(kz.bsbnb.common.util.Constants.DATE_FORMAT);

        Decision result = new Decision();
        Question question = questionRepository.findOne(bean.getQuestionId());
        Answer answer = null;
        if (bean.getAnswerId() != null) {
            answer = answerRepository.findOne(bean.getAnswerId());
        }
        Voter voter = voterRepository.findOne(bean.getVoterId());
        Date d = format.parse(bean.getDateCreate());
        if (d == null) {
            d = new Date();
        }
        if (bean.getId() == null) {
            result.setAnswer(answer);
            result.setComments(bean.getComments());

            result.setDateCreate(d);
            result.setScore(bean.getScore());
            result.setVoter(voter);
        } else {
            result = decisionRepository.findOne(bean.getId());
            result.setAnswer(answer);
            result.setComments(bean.getComments());
            result.setDateCreate(d);
            result.setScore(bean.getScore());
            result.setVoter(voter);
        }
        return result;
    }




    @Override
    @RequestMapping(value = "/readyForOper/{userId}", method = RequestMethod.GET)
    public List<VotingBean> getReadyForOperVotings(@PathVariable Long userId) {
        User user = userRepository.findOne(userId);
        List<VotingBean> result = new ArrayList<>();
        if (user != null) {
            for (UserRoles userRoles : user.getUserRolesSet()) {
                List<Voting> vots = votingRepository.getByOrganisation(userRoles.getOrganisation());
                for (Voting voting : vots) {
//                    if (voting.getStatus().equals("NEW") || voting.getStatus().equals("CREATED")) {
                    boolean isFound = false;
                    for (VotingBean bean : result) {
                        if (bean.getId().equals(voting.getId())) {
                            isFound = true;
                        }
                    }
                    if (!isFound) {
                        result.add(userController.castToBean(voting, user));
                    }
//                    }
                }
            }
        }
        return result;
    }

    @Override
    @RequestMapping(value = "/reportWord/{votingId}", method = RequestMethod.GET)
    public void getVotingQuestions(@PathVariable Long votingId,
                                   HttpServletResponse response) {
        String filepath = "C:\\test\\";
        //String filepath = "/opt/voting/test/";
        Voting voting = votingRepository.findOne(votingId);
        if (voting != null && voting.getDateClose() != null) {
            Map<String, String> map = new HashMap<>();
            map.put("organization_name", voting.getOrganisation().getOrganisationName());
            List<Attribute> attrs = attributeRepository.findByObjectAndObjectId("ORG", voting.getOrganisation().getId());
            String addr = attributeProcessor.getValue(attrs, "ADDRESS");

            map.put("organization_address", addr == null ? "Адрес не указан" : addr);
            SimpleDateFormat ft = new SimpleDateFormat("dd/MM/yyyy");
            SimpleDateFormat ftLong = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            map.put("voting_endDate", ft.format(voting.getDateClose()) + " год");
            map.put("date_begin", ftLong.format(voting.getDateBegin()));
            map.put("date_begin_short", ft.format(voting.getDateBegin()) + " год");
            map.put("date_end", ftLong.format(voting.getDateEnd()));
            if (voting.getLastReestrId() != null) {
                ReestrHead reestr = reestrHeadRepository.findOne(voting.getLastReestrId());
                map.put("reestr_date", ft.format(reestr.getDateCreate()));
            } else {
                map.put("reestr_date", "без реестра");
            }

            Long realCount = 0L;
            Long voterCount = 0L;
            Long votingShares = 0L;
            for (Voter voter : voting.getVoterSet()) {
//                votingShares += voter.getShareCount();
//                if (!voter.getDecisionSet().isEmpty()) {
//                    for (Decision d : voter.getDecisionSet()) {
//                        if (!d.getStatus().equals("KILLED")) {
//                            realCount = realCount + voter.getShareCount();
//
//                        }
//                    }
//                }
                voterCount++;
            }


            if (voting.getKvoroom() != null && voting.getKvoroom()) {
                //voterCount = voting.getOrganisation().getAllShareCount();
            }

            map.put("total_count", StringUtil.parseToStr(voterCount));

            map.put("real_count", StringUtil.parseToStr(votingShares));
            map.put("total_count_text", ConvertUtil.digits2Text(voterCount.doubleValue()));

            map.put("real_count_text", ConvertUtil.digits2Text(votingShares.doubleValue()));
            //map.put("prc_count", (int) (votingShares.doubleValue() / voting.getOrganisation().getAllShareCount().doubleValue() * 100) + "");

            String str = "";
            /*
            for (Question question : voting.getQuestionSet()) {
                str = str + "\n" + question.getNum().toString();
                str = str + "\nФормулировка решения, поставленного на голосование:\n";
                str = str + "\"" + question.getQuestion() + "\".\n";
                str = str + "Итоги голосования:\n";
                if (question.getDecision() != null) {
                    try {
                        System.out.println("question.getDecision()=" + question.getDecision());
                        List<Map> list = (List<Map>) JsonUtil.fromJson(question.getDecision(), List.class);

                        for (Map totalDecision : list) {
                            System.out.println(totalDecision);
                            try {
                                str = str + "\"" + totalDecision.get("answerText").toString() + "\"\t–\t" + totalDecision.get(question.getQuestionType().equals("ORDINARY") ? "answerCount" : "answerScore").toString() + " голос (-а, -ов)\n";
                            } catch (Exception e) {
                                str = str + "\n";
                            }
                        }
                        str = str + "Решение принято\n\n";
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    str = str + "Голосов нет\n";
                }
            }
            */
            for (Question question : voting.getQuestionSet()) {
                str = str + "\n" + question.getNum().toString();
                str = str + "\nФормулировка решения, поставленного на голосование:\n";
                QuestionMessage questionMessage = question.getMessage(messageProcessor.getCurrentLocale());
                str = str + "\"" + questionMessage == null ? null : questionMessage.getText() + "\".\n";
                str = str + "Итоги голосования:\n";
                List<SimpleDecisionBean> totalDecisions = userController.getDecisionsForQuestion(question.getId());
                for (SimpleDecisionBean res : totalDecisions) {
                    str = str + "\"" + res.getAnswerText().toString() + "\"\t–\t" + res.getTotalScore().toString() + " голос (-а, -ов)\n";
                }

            }

            map.put("decision_text", str);

            String tempName = "";
            if (voting.getKvoroom()) {
                tempName = filepath + "test.docx";
            } else {
                tempName = filepath + "test2.docx";
            }
            String fileName = WordUtil.fill(map, votingId, tempName);

            if (fileName == null) {
                if (voting.getKvoroom()) {
                    fileName = filepath + "test.docx";
                } else {
                    fileName = filepath + "test2.docx";
                }
            } else {
                fileName = filepath + fileName;
            }
            File file = new File(fileName);
            if (file.exists() && !file.isDirectory()) {
                try {
                    // get your file as InputStream
                    // do something

                    InputStream is = new FileInputStream(fileName);
                    // copy it to response's OutputStream
                    org.apache.commons.io.IOUtils.copy(is, response.getOutputStream());
                    response.flushBuffer();
                } catch (IOException ex) {
                    throw new RuntimeException("IOError writing file to output stream");
                }
            } else {
                try {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
                } catch (IOException e) {
                    throw new RuntimeException("IOError writing file to output stream");
                }

            }
        }
    }

    private Boolean calcKvoroom(Voting voting) {
        Long all = 0L;
        Set<Share> shares = voting.getOrganisation().getShares();
        for(Share share: shares) {
            all += share.getAmount();
        }
        Long vote = 0L;
        if (voting.getVoterSet() != null) {
            for (Voter voter : voting.getVoterSet()) {
                if (voter.getDecisionSet() != null) {
                    boolean approved = false;
                    for (Decision decision : voter.getDecisionSet()) {
                        if (decision.getStatus().equals("READY")) {
                            approved = true;
                        }
                    }
                    if (approved) {
//                        vote = vote + voter.getShareCount();
                    }
                }
            }
        }
        return vote * 2 > (all == null ? 0 : all);
    }


}