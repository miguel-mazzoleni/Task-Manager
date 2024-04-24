import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

enum TaskStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED
}

class Task {
    private int id;
    private String title;
    private String description;
    private String assignee;
    private TaskStatus status;
    private Date creationDate;
    private Date completionDate;

    public Task(int id, String title, String description, String assignee) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.assignee = assignee;
        this.status = TaskStatus.PENDING;
        this.creationDate = new Date();
        this.completionDate = null;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getAssignee() {
        return assignee;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public Date getCompletionDate() {
        return completionDate;
    }

    public void startTask() {
        this.status = TaskStatus.IN_PROGRESS;
    }

    public void completeTask() {
        this.status = TaskStatus.COMPLETED;
        this.completionDate = new Date();
    }
}

class User {
    private String username;
    private String password;
    private List<Task> assignedTasks;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.assignedTasks = new ArrayList<>();
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public List<Task> getAssignedTasks() {
        return assignedTasks;
    }

    public void assignTask(Task task) {
        assignedTasks.add(task);
    }
}

public class TaskManager {
    private static List<Task> tasks = new ArrayList<>();
    private static Connection connection = null;
    private static Scanner scanner = new Scanner(System.in);
    private static User currentUser;

    public static void main(String[] args) {
        initializeDatabase();
        System.out.println("Bem-vindo ao sistema de gerenciamento de tarefas!");

        // Opções de login ou cadastro
        System.out.println("Opções:");
        System.out.println("1. Login");
        System.out.println("2. Cadastro");
        System.out.print("Escolha uma opção: ");

        int choice = scanner.nextInt();
        scanner.nextLine();

        switch (choice) {
            case 1:
                currentUser = login();
                if (currentUser != null) {
                    System.out.println("Login bem-sucedido!");
                } else {
                    System.out.println("Login falhou. Encerrando...");
                    return;
                }
                break;
            case 2:
                register();
                break;
            default:
                System.out.println("Opção inválida. Encerrando...");
                return;
        }

        while (true) {
            // Menu
            System.out.println("\nOpções:");
            System.out.println("1. Criar nova tarefa");
            System.out.println("2. Listar tarefas");
            System.out.println("3. Ver Minhas Tarefas");
            System.out.println("4. Marcar Tarefa como Concluída");
            System.out.println("5. Sair");
            System.out.print("Escolha uma opção: ");

            int option = scanner.nextInt();
            scanner.nextLine();

            switch (option) {
                case 1:
                    createTask();
                    break;
                case 2:
                    listTasks();
                    break;
                case 3:
                    viewMyTasks(currentUser);
                    break;
                case 4:
                    markTaskAsCompleted(currentUser);
                    break;
                case 5:
                    System.out.println("Saindo...");
                    return;
                default:
                    System.out.println("Opção inválida. Tente novamente.");
            }
        }
    }

    private static void initializeDatabase() {
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:task_manager.db");

            String createUserTableSQL = "CREATE TABLE IF NOT EXISTS users (" +
                                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                                        "username TEXT NOT NULL UNIQUE," +
                                        "password TEXT NOT NULL)";
            connection.createStatement().executeUpdate(createUserTableSQL);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static User login() {
        System.out.print("Digite seu nome de usuário: ");
        String username = scanner.nextLine();

        System.out.print("Digite sua senha: ");
        String password = scanner.nextLine();

        try {
            String selectUserSQL = "SELECT * FROM users WHERE username = ? AND password = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(selectUserSQL);
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, password);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return new User(username, password);
            } else {
                return null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static void register() {
        System.out.print("Digite um nome de usuário: ");
        String username = scanner.nextLine();

        System.out.print("Digite uma senha: ");
        String password = scanner.nextLine();

        try {
            String insertUserSQL = "INSERT INTO users (username, password) VALUES (?, ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(insertUserSQL);
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, password);
            preparedStatement.executeUpdate();

            System.out.println("Cadastro realizado com sucesso!");
        } catch (SQLException e) {
            System.out.println("Erro ao cadastrar usuário. Verifique se o nome de usuário já existe.");
        }
    }

    private static void createTask() {
        System.out.print("Digite o titulo da tarefa: ");
        String title = scanner.nextLine();

        System.out.print("Digite a descrição da tarefa: ");
        String description = scanner.nextLine();

        System.out.print("Digite o nome do responsável pela tarefa: ");
        String assignee = scanner.nextLine();

        Task newTask = new Task(tasks.size() + 1, title, description, assignee);
        tasks.add(newTask);

        System.out.println("Tarefa criada com sucesso!");
    }

    private static void listTasks() {
        System.out.println("\nLista de Tarefas:");

        for (Task task : tasks) {
            System.out.println("ID: " + task.getId());
            System.out.println("Ti­tulo: " + task.getTitle());
            System.out.println("Descrição: " + task.getDescription());
            System.out.println("Responsável: " + task.getAssignee());
            System.out.println("Data de Criação: " + task.getCreationDate());
            System.out.println("Status: " + task.getStatus());
            System.out.println("----------------------------------");
        }
    }

    private static void viewMyTasks(User currentUser) {
        System.out.println("\nMinhas Tarefas:");
        boolean foundTasks = false;
        for (Task task : tasks) {
            if (task.getAssignee().equals(currentUser.getUsername())) {
                foundTasks = true;
                System.out.println("ID: " + task.getId());
                System.out.println("Ti­tulo: " + task.getTitle());
                System.out.println("Descrição: " + task.getDescription());
                System.out.println("Responsável: " + task.getAssignee());
                System.out.println("Data de Criação: " + task.getCreationDate());
                System.out.println("Status: " + task.getStatus());
                System.out.println("----------------------------------");
            }
        }
        if (!foundTasks) {
            System.out.println("Nenhuma tarefa encontrada para você.");
        }
    }

private static void markTaskAsCompleted(User currentUser) {
    listTasks();

    System.out.print("Digite o ID da tarefa que deseja marcar como concluída: ");
    int taskId = scanner.nextInt();
    scanner.nextLine();

    Task taskToComplete = null;
    for (Task task : tasks) {
        if (task.getId() == taskId) {
            if (task.getAssignee().equals(currentUser.getUsername())) {
                taskToComplete = task;
                break;
            } else {
                System.out.println("Tarefa encontrada, mas não atribuída a você.");
                return;
            }
        }
    }

    if (taskToComplete != null) {
        taskToComplete.completeTask();
        // Atualiza o status da tarefa diretamente na lista tasks
        tasks.set(taskId - 1, taskToComplete);
        System.out.println("Tarefa marcada como concluída com sucesso!");
    } else {
        System.out.println("Tarefa não encontrada.");
    }
}

}
