package service;

import model.Student;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

// 点名系统业务逻辑类：处理文件的读写、点名算法的筛选以及结果的更新
public class RollCallService {
    private final List<Student> studentList = new ArrayList<>();
    private final String FILE_PATH = "students.txt";
    private final Random random = new Random();

    // 连续答错的人数
    private int continuousFailCount = 0;
    // 触发特殊模式的阈值
    private final int N = 3;
    // 当前选中的学生
    private Student luckyGuy = null;

    // 本轮答错的学生ID黑名单
    private final List<String> failedIdsInThisRound = new ArrayList<>();
    // 是否全班都不会的警报状态
    private boolean isAllFailedWarning = false;

    public RollCallService() {
        loadStudents();
    }

    // 从本地 txt 文件读取学生数据
    public synchronized void loadStudents() {
        studentList.clear();
        File file = new File(FILE_PATH);
        // 如果文件不存在，初始化默认的10人名单
        if (!file.exists()) {
            resetToDefault();
            return;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                String[] parts = line.split(",");
                if (parts.length == 4) {
                    studentList.add(new Student(
                            parts[0].trim(),
                            parts[1].trim(),
                            Integer.parseInt(parts[2].trim()),
                            Integer.parseInt(parts[3].trim())
                    ));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 将当前内存中的学生数据保存到 txt 文件
    public synchronized void saveStudents() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_PATH))) {
            for (Student s : studentList) {
                bw.write(s.getId() + "," + s.getName() + "," + s.getCallCount() + "," + s.getRightCount());
                bw.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 重置所有数据为默认名单
    public synchronized void resetToDefault() {
        studentList.clear();
        continuousFailCount = 0;
        luckyGuy = null;
        failedIdsInThisRound.clear();
        isAllFailedWarning = false;

        String[] names = {"张三","李四","王五","赵六","钱七","孙八","周九","吴十","郑十一","王十二"};
        for (int i = 0; i < names.length; i++) {
            studentList.add(new Student(String.valueOf(1001 + i), names[i], 0, 0));
        }
        saveStudents();
    }

    private double getRatio(Student s) {
        return s.getCallCount() == 0 ? 0.0 : (double) s.getRightCount() / s.getCallCount();
    }

    // 核心点名算法
    public synchronized Student executeRollCall() {
        if (studentList.isEmpty()) {
            return null;
        }

        isAllFailedWarning = false;

        // 分支 A：如果连续错满 3 次及以上，启用正确率高的“学霸模式”
        if (continuousFailCount >= N) {
            List<Student> sortedElites = studentList.stream()
                    // 过滤掉本题已经错过的同学
                    .filter(s -> !failedIdsInThisRound.contains(s.getId()))
                    .sorted((s1, s2) -> {
                        double r1 = getRatio(s1);
                        double r2 = getRatio(s2);
                        if (Math.abs(r1 - r2) > 0.00001) {
                            // 正确率高的排前面
                            return Double.compare(r2, r1);
                        }
                        // 次数少的排前面
                        return Integer.compare(s1.getCallCount(), s2.getCallCount());
                    })
                    .collect(Collectors.toList());

            if (!sortedElites.isEmpty()) {
                luckyGuy = sortedElites.get(0);
            } else {
                // 如果过滤后没人了，说明全班都在这道题上答错了
                isAllFailedWarning = true;
                failedIdsInThisRound.clear();
                continuousFailCount = 0;
                luckyGuy = null;
                return null;
            }

            // 分支 B：正常情况下，执行最少点名次数的公平随机抽取
        } else {
            int minCall = Integer.MAX_VALUE;
            for (Student s : studentList) {
                if (s.getCallCount() < minCall) {
                    minCall = s.getCallCount();
                }
            }

            List<Student> fairPool = new ArrayList<>();
            for (Student s : studentList) {
                if (s.getCallCount() == minCall) {
                    fairPool.add(s);
                }
            }

            luckyGuy = fairPool.get(random.nextInt(fairPool.size()));
        }

        luckyGuy.setCallCount(luckyGuy.getCallCount() + 1);
        saveStudents();
        return luckyGuy;
    }

    // 处理点名回答结果
    public synchronized void handleResult(String studentId, boolean isCorrect) {
        for (Student s : studentList) {
            if (s.getId().equals(studentId)) {
                if (isCorrect) {
                    s.setRightCount(s.getRightCount() + 1);
                    continuousFailCount = 0;
                    // 答对后清空本题黑名单
                    failedIdsInThisRound.clear();
                } else {
                    continuousFailCount++;
                    // 答错则加入黑名单
                    failedIdsInThisRound.add(studentId);
                }
                saveStudents();
                break;
            }
        }
        // 答完后将当前状态置为空，等待下一次抽取
        this.luckyGuy = null;
    }

    public List<Student> getStudentList() { return studentList; }
    public int getContinuousFailCount() { return continuousFailCount; }
    public int getN() { return N; }
    public Student getLuckyGuy() { return luckyGuy; }
    public boolean isAllFailedWarning() { return isAllFailedWarning; }
}