package org.activiti.test.financial;

import java.util.List;

import org.activiti.engine.HistoryService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class FinancialTestLone {
	public static void main(String[] args) {

		// 在测试的时候没有使用junit
		ProcessEngine processEngine = ProcessEngineConfiguration
				.createProcessEngineConfigurationFromResource("activiti.lone.mysql.cfg.xml")
				.buildProcessEngine();

		FinancialTestLone t = new FinancialTestLone();
		// step 1 部署
		t.deploymentInstance(processEngine);
		// step 2 启动流程实例
		t.startInstance(processEngine);
		// step 3 查询用户任务
		t.queryUserTask(processEngine);
		// step 4 分配任何给user
		t.claimTask(processEngine);
		// step 5 查询个人任务列表
		 t.queryPersonalTaskList(processEngine);
		// step 6 完成任务
		 t.completePersonalTask(processEngine);
		// step 7 查询历史流程信息
		 t.queryHistoryProcessInstance(processEngine);
		 
		 // 流程下一级操作
		 t.queryPersonalTaskListT(processEngine);
		 t.queryHistoryProcessInstance(processEngine);
	}

	// 部署流程实例
	public void deploymentInstance(ProcessEngine processEngine) {
		// 获得repositoryService
		RepositoryService repositoryService = processEngine
				.getRepositoryService();
		// 从文件部署流程
		Deployment deployment = repositoryService.createDeployment()
				.addClasspathResource("FinancialReportProcess.bpmn20.xml")
				.deploy();

	}

	// 启动流程
	public void startInstance(ProcessEngine processEngine) {
		// 获得 runtimeservice对象
		RuntimeService runtimeService = processEngine.getRuntimeService();
		// 启动流程实例 ,注意这里的key是我们流程文件中的<process id="financialReport"
		// ,id属性,在Activiti术语叫key
		ProcessInstance processInstance = runtimeService
				.startProcessInstanceByKey("financialReport");

	}

	// 查询组任务
	public void queryUserTask(ProcessEngine processEngine) {
		// 获得 TaskService 对象
		TaskService taskService = processEngine.getTaskService();

		// 查询任务列表使用组名称
		List<Task> tasks02 = taskService.createTaskQuery()
				.taskCandidateGroup("accountancy").list();
		for (Task task : tasks02) {
			System.out.println(task.getName());
		}
	}

	// 分配任务给用户
	public void claimTask(ProcessEngine processEngine) {
		// 获得TaskService对象
		TaskService taskService = processEngine.getTaskService();
		// 得到组任务,也可以是 management组
		List<Task> tasks02 = taskService.createTaskQuery()
				.taskCandidateGroup("accountancy").list();
		// 任务分配之后这个任务就会从accountancy用户组的任务列表中消失
		for (Task task2 : tasks02) {
			taskService.claim(task2.getId(), "fozzie");
			System.out.println("任务名称:" + task2.getName() + ": taskId :"
					+ task2.getId());
		}

		// 此时查询fozzie人的个人任务列表
		List<Task> tasks = taskService.createTaskQuery().taskAssignee("fozzie")
				.list();
		for (Task t : tasks) {
			System.out.println("已受理任务名称:" + t.getName() + "TaskId : "
					+ t.getId() + " " + t.getAssignee());
		}

	}

	// 查询个人的任务列表
	public void queryPersonalTaskList(
			ProcessEngine processEngine) {
		// 获得TaskService对象
		TaskService taskService =  processEngine.getTaskService();
		// 查询fozzie用户的个人任务列表
		List<Task> tasks = taskService.createTaskQuery().taskAssignee("fozzie")
				.list();
		// 输出受理任务信息
		for (Task t : tasks) {
			System.out.println("已受理任务名称:" + t.getName());
		}
	}

	// 完成任务
	public void completePersonalTask(
			ProcessEngine processEngine) {
		// 获得TaskService对象
		TaskService taskService =  processEngine.getTaskService();

		// 查询 fozzie用户个人任务列表并完成其任务
		List<Task> tasks = taskService.createTaskQuery().taskAssignee("fozzie")
				.list();

		// 完成用户任务
		for (Task task : tasks) {
			System.out.println("完成任务名称:" + task.getName());
			// 通过任务id完成任务
			taskService.complete(task.getId());
		}

	}

	// 查询历史流程信息,也许你在查询的时候这个任务没有结束
	// 那么请你将management组的任务claimTask分配给用户然后completePersonalTask完成任务
	// 这个流程实例就算完成了,你在这里也才会查询出来,否则流程实例没有到达
	public void queryHistoryProcessInstance(
			ProcessEngine processEngine) {
		// 获取historyService
		HistoryService historyService =  processEngine.getHistoryService();
		
		// 在这里需要注意的是,你的financialReport流程如果启动多个,singleResult将会出错
		// 由于这里是测试我很清除这个实例只启动了一个,所以使用singleResult方法,如果你在测试时候需要注意
		// HistoricProcessInstance historicProcessInstance =
		// historyService.createHistoricProcessInstanceQuery().processDefinitionKey("financialReport").singleResult();//processInstanceId("financialReport").singleResult();

		List<HistoricProcessInstance> historicProcessInstances = historyService
				.createHistoricProcessInstanceQuery()
				.processDefinitionKey("financialReport").list();
		for (HistoricProcessInstance historicProcessInstance : historicProcessInstances) {
			System.out.println("流程结束时间: "
					+ historicProcessInstance.getEndTime());
		}

	}

	// managent Opertation
	public void queryPersonalTaskListT(
ProcessEngine processEngine) {

		TaskService taskService = processEngine.getTaskService();

		// 查询任务列表使用组名称
		List<Task> tasks02 = taskService.createTaskQuery()
				.taskCandidateGroup("management").list();
		for (Task task : tasks02) {
			System.out.println(task.getName());
			taskService.claim(task.getId(), "kermit"); // 认领任务
			System.out.println("任务名称:" + task.getName() + ": taskId :"
					+ task.getId());
		}

		// 查询kermit用户的个人任务列表
		List<Task> tasks = taskService.createTaskQuery().taskAssignee("kermit")
				.list();
		// 输出受理任务信息
		for (Task t : tasks) {
			System.out.println("已受理任务名称:" + t.getName());
		}

		// 处理任务，并标识为已处理完
		for (Task t : tasks) {
			System.out.println("完成任务名称:" + t.getName());
			// 通过任务id完成任务
			taskService.complete(t.getId());
		}

	}

}
