package model;

// 学生实体类：存放学生的基本信息、点名次数和正确次数
public class Student {
    // 学号
    private String id;
    // 姓名
    private String name;
    // 被点名总次数
    private int callCount;
    // 回答正确的次数
    private int rightCount;

    // 构造方法
    public Student(String id, String name, int callCount, int rightCount) {
        this.id = id;
        this.name = name;
        this.callCount = callCount;
        this.rightCount = rightCount;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public int getCallCount() { return callCount; }
    public void setCallCount(int callCount) { this.callCount = callCount; }
    public int getRightCount() { return rightCount; }
    public void setRightCount(int rightCount) { this.rightCount = rightCount; }

    // 计算答对率，保留两位小数
    public String getRightRatio() {
        if (callCount == 0) {
            return "0.00%";
        }
        return String.format("%.2f%%", ((double) rightCount / callCount) * 100);
    }
}