package cocoffee.models;

public class Invoice {
    private int id;
    private String invoiceCode;
    private int employeeId; // Không còn tableId
    private double subtotal;
    private double discount;
    private double total;
    private String paymentMethod;
    private String status; // 'OPEN', 'PAID', 'CANCELLED'
    private String createdAt;
    private String paidAt;

    public Invoice() {}

    public Invoice(int id, String invoiceCode, int employeeId, double subtotal, double discount, double total, String paymentMethod, String status, String createdAt, String paidAt) {
        this.id = id;
        this.invoiceCode = invoiceCode;
        this.employeeId = employeeId;
        this.subtotal = subtotal;
        this.discount = discount;
        this.total = total;
        this.paymentMethod = paymentMethod;
        this.status = status;
        this.createdAt = createdAt;
        this.paidAt = paidAt;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getInvoiceCode() { return invoiceCode; }
    public void setInvoiceCode(String invoiceCode) { this.invoiceCode = invoiceCode; }

    public int getEmployeeId() { return employeeId; }
    public void setEmployeeId(int employeeId) { this.employeeId = employeeId; }

    public double getSubtotal() { return subtotal; }
    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }

    public double getDiscount() { return discount; }
    public void setDiscount(double discount) { this.discount = discount; }

    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getPaidAt() { return paidAt; }
    public void setPaidAt(String paidAt) { this.paidAt = paidAt; }
}