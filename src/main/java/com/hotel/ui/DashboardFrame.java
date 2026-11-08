package com.hotel.ui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;

import com.hotel.dao.BookingDAO;
import com.hotel.dao.CustomerDAO;
import com.hotel.dao.InvoiceDAO;
import com.hotel.dao.PaymentDAO;
import com.hotel.dao.ReportDAO;
import com.hotel.dao.RoomDAO;
import com.hotel.model.Booking;
import com.hotel.model.BookingDetail;
import com.hotel.model.ChartPoint;
import com.hotel.model.Customer;
import com.hotel.model.Invoice;
import com.hotel.model.ReportSummary;
import com.hotel.model.Room;
import com.hotel.model.RoomType;
import com.hotel.patterns.facade.BookingFacade;
import com.hotel.patterns.observer.BookingNotifier;
import com.hotel.service.AuthService;
import com.hotel.service.BookingService;
import com.hotel.service.CustomerService;
import com.hotel.service.PaymentService;
import com.hotel.service.ReportService;
import com.hotel.service.RoomService;
import com.hotel.ui.components.BarChartPanel;
import com.hotel.ui.components.NavButton;
import com.hotel.ui.components.StatCard;
import com.hotel.ui.components.StatusCellRenderer;
import com.hotel.ui.components.WrapLayout;
import com.hotel.ui.theme.AppTheme;
import com.hotel.ui.theme.AppTheme.RoundedPanel;

public class DashboardFrame extends JFrame {
    private static final String HOME = "home";
    private static final String ROOMS = "rooms";
    private static final String CUSTOMERS = "customers";
    private static final String BOOKINGS = "bookings";
    private static final String INVOICES = "invoices";
    private static final String REPORTS = "reports";

    private final AuthService.Session session;
    private final RoomService roomService = new RoomService(new RoomDAO());
    private final CustomerService customerService = new CustomerService(new CustomerDAO());
    private final ReportService reportService = new ReportService(new ReportDAO());
    private final BookingDAO bookingDAO = new BookingDAO();
    private final BookingService bookingService;
    private final PaymentService paymentService;
    private final BookingNotifier notifier = new BookingNotifier();

    private final CardLayout pages = new CardLayout();
    private final JPanel content = new JPanel(pages);
    private final JLabel pageTitle = AppTheme.heading("Tổng quan");
    private final JLabel pageSubtitle = AppTheme.muted("Tình hình hoạt động hôm nay");
    private final java.util.List<NavButton> navButtons = new java.util.ArrayList<>();

    private StatCard roomsCard;
    private StatCard availableCard;
    private StatCard customersCard;
    private StatCard bookingsCard;
    private JTable roomsTable;
    private JTable customersTable;
    private JTable bookingsTable;
    private JTable invoicesTable;
    private JTable recentBookingsTable;
    private JPanel roomStatusList;
    private JLabel revenueLabel;

    private final java.util.List<Room> allRooms = new java.util.ArrayList<>();
    private final java.util.List<Customer> allCustomers = new java.util.ArrayList<>();
    private final java.util.List<Booking> allBookings = new java.util.ArrayList<>();
    private final java.util.List<Invoice> allInvoices = new java.util.ArrayList<>();
    private JTextField roomSearch;
    private JComboBox<String> roomStatusFilter;
    private JTextField customerSearch;
    private JTextField bookingSearch;
    private JComboBox<String> bookingStatusFilter;
    private JTextField invoiceSearch;
    private JTextField reportFrom;
    private JTextField reportTo;
    private BarChartPanel reportChart;
    private JLabel reportSummaryLabel;

    public DashboardFrame(AuthService.Session session) {
        this.session = session;
        notifier.subscribe(message -> SwingUtilities.invokeLater(() ->
                JOptionPane.showMessageDialog(this, message, "Thông báo", JOptionPane.INFORMATION_MESSAGE)));
        this.paymentService = new PaymentService(new PaymentDAO(), new InvoiceDAO(), bookingDAO);
        this.bookingService = new BookingService(bookingDAO, new BookingFacade(bookingDAO, paymentService, notifier));

        setTitle("Lumina Hotel — " + session.fullName());
        setSize(1180, 720);
        setMinimumSize(new Dimension(980, 620));
        setExtendedState(MAXIMIZED_BOTH);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel root = new JPanel(new BorderLayout());
        root.add(sidebar(), BorderLayout.WEST);
        root.add(mainArea(), BorderLayout.CENTER);
        setContentPane(root);
        showPage(HOME, "Tổng quan", "Tình hình hoạt động hôm nay");
        refreshHome();
    }

    private JPanel sidebar() {
        JPanel side = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0, 0, AppTheme.SIDEBAR, 0, getHeight(), AppTheme.SIDEBAR_DEEP));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(new Color(203, 165, 47, 26));
                g2.fillOval(-90, getHeight() - 200, 240, 240);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        side.setOpaque(false);
        side.setPreferredSize(new Dimension(240, 0));
        side.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(255, 255, 255, 16)),
                BorderFactory.createEmptyBorder(22, 16, 20, 16)));

        RoundedPanel logo = new RoundedPanel(12, AppTheme.GOLD);
        logo.setLayout(new GridBagLayout());
        logo.setPreferredSize(new Dimension(40, 40));
        logo.setMinimumSize(new Dimension(40, 40));
        logo.setMaximumSize(new Dimension(40, 40));
        JLabel glyph = new JLabel("◆");
        glyph.setFont(new Font("Segoe UI", Font.BOLD, 15));
        glyph.setForeground(AppTheme.SIDEBAR_DEEP);
        logo.add(glyph);

        JLabel brandName = new JLabel("LUMINA HOTEL");
        brandName.setFont(new Font("Segoe UI", Font.BOLD, 15));
        brandName.setForeground(Color.WHITE);
        JLabel brandSub = new JLabel("Hotel Manager");
        brandSub.setFont(AppTheme.SMALL);
        brandSub.setForeground(new Color(148, 163, 184));
        JPanel brandText = new JPanel();
        brandText.setOpaque(false);
        brandText.setLayout(new BoxLayout(brandText, BoxLayout.Y_AXIS));
        brandName.setAlignmentX(Component.LEFT_ALIGNMENT);
        brandSub.setAlignmentX(Component.LEFT_ALIGNMENT);
        brandText.add(brandName);
        brandText.add(Box.createVerticalStrut(2));
        brandText.add(brandSub);

        JPanel brand = new JPanel(new BorderLayout(12, 0));
        brand.setOpaque(false);
        brand.add(logo, BorderLayout.WEST);
        brand.add(brandText, BorderLayout.CENTER);
        brand.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 0));

        JLabel section = new JLabel("ĐIỀU HƯỚNG");
        section.setFont(new Font("Segoe UI", Font.BOLD, 11));
        section.setForeground(new Color(120, 138, 160));
        section.setBorder(BorderFactory.createEmptyBorder(0, 8, 8, 0));
        section.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel nav = new JPanel();
        nav.setOpaque(false);
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
        nav.add(Box.createVerticalStrut(24));
        nav.add(section);
        nav.add(navItem("Tổng quan", HOME, "Tổng quan", "Tình hình hoạt động hôm nay"));
        nav.add(Box.createVerticalStrut(6));
        nav.add(navItem("Phòng", ROOMS, "Phòng", "Danh sách và trạng thái phòng"));
        nav.add(Box.createVerticalStrut(6));
        nav.add(navItem("Khách hàng", CUSTOMERS, "Khách hàng", "Hồ sơ khách lưu trú"));
        nav.add(Box.createVerticalStrut(6));
        nav.add(navItem("Đặt phòng", BOOKINGS, "Đặt phòng", "Tạo, theo dõi và hủy đặt phòng"));
        nav.add(Box.createVerticalStrut(6));
        nav.add(navItem("Hóa đơn", INVOICES, "Hóa đơn", "Danh sách hóa đơn và doanh thu"));
        nav.add(Box.createVerticalStrut(6));
        nav.add(navItem("Báo cáo", REPORTS, "Báo cáo doanh thu", "Doanh thu theo ngày/tháng/loại phòng và công suất"));

        aiSetAlignmentX(nav, Component.LEFT_ALIGNMENT);

        RoundedPanel user = new RoundedPanel(14, AppTheme.SIDEBAR_HOVER);
        user.setLayout(new BorderLayout(10, 4));
        user.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        RoundedPanel avatar = new RoundedPanel(12, AppTheme.GOLD);
        avatar.setLayout(new GridBagLayout());
        avatar.setPreferredSize(new Dimension(38, 38));
        avatar.setMinimumSize(new Dimension(38, 38));
        JLabel initials = new JLabel(initials(session.fullName()));
        initials.setFont(AppTheme.SUBHEADING);
        initials.setForeground(AppTheme.SIDEBAR_DEEP);
        avatar.add(initials);
        JLabel uname = new JLabel(session.fullName());
        uname.setFont(AppTheme.SUBHEADING);
        uname.setForeground(Color.WHITE);
        JLabel role = new JLabel(session.role());
        role.setFont(AppTheme.SMALL);
        role.setForeground(new Color(148, 163, 184));
        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        uname.setAlignmentX(Component.LEFT_ALIGNMENT);
        role.setAlignmentX(Component.LEFT_ALIGNMENT);
        info.add(uname);
        info.add(role);
        user.add(avatar, BorderLayout.WEST);
        user.add(info, BorderLayout.CENTER);

        JButton logout = AppTheme.ghostButton("Đăng xuất");
        for (JButton b : new JButton[]{logout}) {
            b.setBackground(AppTheme.SIDEBAR_ACTIVE);
            b.setForeground(Color.WHITE);
            b.setHorizontalAlignment(SwingConstants.CENTER);
            b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
            b.setAlignmentX(Component.LEFT_ALIGNMENT);
        }
        logout.addActionListener(e -> {
            dispose();
            new LoginFrame().setVisible(true);
        });

        JPanel bottom = new JPanel();
        bottom.setOpaque(false);
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));
        user.setAlignmentX(Component.LEFT_ALIGNMENT);
        bottom.add(user);
        bottom.add(Box.createVerticalStrut(12));
        bottom.add(logout);

        side.add(brand, BorderLayout.NORTH);
        side.add(nav, BorderLayout.CENTER);
        side.add(bottom, BorderLayout.SOUTH);
        return side;
    }

    private void aiSetAlignmentX(JPanel panel, float alignment) {
        for (Component c : panel.getComponents()) {
            if (c instanceof JComponent jc) jc.setAlignmentX(alignment);
        }
    }

    private NavButton navItem(String text, String key, String title, String subtitle) {
        NavButton b = new NavButton("   " + text);
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        b.setAlignmentX(Component.LEFT_ALIGNMENT);
        b.addActionListener(e -> showPage(key, title, subtitle));
        navButtons.add(b);
        b.putClientProperty("page", key);
        return b;
    }

    private void showPage(String key, String title, String subtitle) {
        pages.show(content, key);
        pageTitle.setText(title);
        pageSubtitle.setText(subtitle);
        for (NavButton b : navButtons) {
            b.setActive(key.equals(b.getClientProperty("page")));
        }
        if (HOME.equals(key)) refreshHome();
        if (REPORTS.equals(key)) refreshReport();
    }

    private JPanel mainArea() {
        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(AppTheme.CREAM);

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(20, 28, 14, 28));
        JPanel titles = new JPanel();
        titles.setOpaque(false);
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
        pageTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titles.add(pageTitle);
        titles.add(Box.createVerticalStrut(4));
        titles.add(pageSubtitle);

        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", new Locale("vi", "VN")));
        RoundedPanel dateChip = new RoundedPanel(12, AppTheme.CARD);
        dateChip.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
        dateChip.setBorderColor(AppTheme.BORDER);
        dateChip.setBorder(BorderFactory.createEmptyBorder(9, 16, 9, 16));
        JLabel dateLabel = new JLabel(today);
        dateLabel.setFont(AppTheme.SMALL);
        dateLabel.setForeground(AppTheme.MUTED);
        dateChip.add(dateLabel);

        RoundedPanel userChip = new RoundedPanel(12, AppTheme.CARD);
        userChip.setLayout(new BorderLayout(10, 0));
        userChip.setBorderColor(AppTheme.BORDER);
        userChip.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 16));
        RoundedPanel headAvatar = new RoundedPanel(10, AppTheme.SIDEBAR);
        headAvatar.setLayout(new GridBagLayout());
        headAvatar.setPreferredSize(new Dimension(32, 32));
        JLabel headInitials = new JLabel(initials(session.fullName()));
        headInitials.setFont(AppTheme.SMALL);
        headInitials.setForeground(AppTheme.GOLD);
        headAvatar.add(headInitials);
        JPanel headInfo = new JPanel();
        headInfo.setOpaque(false);
        headInfo.setLayout(new BoxLayout(headInfo, BoxLayout.Y_AXIS));
        JLabel headName = new JLabel(session.fullName());
        headName.setFont(AppTheme.SUBHEADING);
        headName.setForeground(AppTheme.TEXT);
        JLabel headRole = new JLabel(session.role());
        headRole.setFont(AppTheme.SMALL);
        headRole.setForeground(AppTheme.MUTED);
        headName.setAlignmentX(Component.LEFT_ALIGNMENT);
        headRole.setAlignmentX(Component.LEFT_ALIGNMENT);
        headInfo.add(headName);
        headInfo.add(headRole);
        userChip.add(headAvatar, BorderLayout.WEST);
        userChip.add(headInfo, BorderLayout.CENTER);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);
        right.add(dateChip);
        right.add(userChip);

        header.add(titles, BorderLayout.WEST);
        header.add(right, BorderLayout.EAST);

        JPanel headerWrap = new JPanel(new BorderLayout());
        headerWrap.setOpaque(false);
        headerWrap.add(header, BorderLayout.CENTER);
        JPanel divider = new JPanel();
        divider.setBackground(AppTheme.BORDER);
        divider.setPreferredSize(new Dimension(0, 1));
        headerWrap.add(divider, BorderLayout.SOUTH);

        content.setOpaque(false);
        content.add(homePanel(), HOME);
        content.add(roomsPanel(), ROOMS);
        content.add(customersPanel(), CUSTOMERS);
        content.add(bookingsPanel(), BOOKINGS);
        content.add(invoicesPanel(), INVOICES);
        content.add(reportsPanel(), REPORTS);

        main.add(headerWrap, BorderLayout.NORTH);
        main.add(content, BorderLayout.CENTER);
        return main;
    }

    private JPanel homePanel() {
        JPanel p = new JPanel(new BorderLayout(0, 16));
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(16, 19, 19, 19));

        roomsCard = new StatCard("Tổng phòng", "—", "Tất cả hạng phòng", AppTheme.SIDEBAR);
        availableCard = new StatCard("Phòng trống", "—", "Sẵn sàng nhận khách", AppTheme.SUCCESS);
        customersCard = new StatCard("Khách hàng", "—", "Đã lưu trong hệ thống", AppTheme.GOLD);
        bookingsCard = new StatCard("Đặt phòng", "—", "Lịch đặt hiện có", AppTheme.INFO);

        JPanel cards = new JPanel(new GridLayout(1, 4, 16, 0));
        cards.setOpaque(false);
        cards.add(roomsCard);
        cards.add(availableCard);
        cards.add(customersCard);
        cards.add(bookingsCard);

        JPanel columns = new JPanel(new GridBagLayout());
        columns.setOpaque(false);

        GridBagConstraints gl = new GridBagConstraints();
        gl.gridx = 0;
        gl.gridy = 0;
        gl.weightx = 0.64;
        gl.weighty = 1;
        gl.fill = GridBagConstraints.BOTH;
        gl.insets = new Insets(0, 0, 0, 16);
        columns.add(recentBookingsCard(), gl);

        GridBagConstraints gr = new GridBagConstraints();
        gr.gridx = 1;
        gr.gridy = 0;
        gr.weightx = 0.36;
        gr.weighty = 1;
        gr.fill = GridBagConstraints.BOTH;
        columns.add(roomStatusCard(), gr);

        p.add(cards, BorderLayout.NORTH);
        p.add(columns, BorderLayout.CENTER);
        return p;
    }

    private RoundedPanel recentBookingsCard() {
        RoundedPanel card = new RoundedPanel(18, AppTheme.CARD, true);
        card.setLayout(new BorderLayout(0, 12));
        card.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel head = new JPanel(new BorderLayout());
        head.setOpaque(false);
        JPanel titles = new JPanel();
        titles.setOpaque(false);
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
        JLabel t = new JLabel("Đặt phòng gần đây");
        t.setFont(AppTheme.SUBHEADING);
        t.setForeground(AppTheme.TEXT);
        titles.add(t);
        titles.add(Box.createVerticalStrut(2));
        titles.add(AppTheme.muted("Các đơn mới nhất trong hệ thống"));
        JButton all = AppTheme.ghostButton("Xem tất cả");
        all.addActionListener(e -> showPage(BOOKINGS, "Đặt phòng", "Tạo, theo dõi và hủy đặt phòng"));
        head.add(titles, BorderLayout.WEST);
        head.add(all, BorderLayout.EAST);

        recentBookingsTable = new JTable();
        AppTheme.styleTable(recentBookingsTable);
        recentBookingsTable.setRowHeight(36);

        card.add(head, BorderLayout.NORTH);
        card.add(AppTheme.wrapTable(recentBookingsTable), BorderLayout.CENTER);
        return card;
    }

    private RoundedPanel roomStatusCard() {
        RoundedPanel card = new RoundedPanel(18, AppTheme.CARD, true);
        card.setLayout(new BorderLayout(0, 12));
        card.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel titles = new JPanel();
        titles.setOpaque(false);
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
        JLabel t = new JLabel("Phòng theo trạng thái");
        t.setFont(AppTheme.SUBHEADING);
        t.setForeground(AppTheme.TEXT);
        titles.add(t);
        titles.add(Box.createVerticalStrut(2));
        titles.add(AppTheme.muted("Tình trạng sử dụng phòng hiện tại"));

        roomStatusList = new JPanel(new GridBagLayout());
        roomStatusList.setOpaque(false);

        card.add(titles, BorderLayout.NORTH);
        card.add(roomStatusList, BorderLayout.CENTER);
        return card;
    }

    private JPanel statusRow(String label, long count, long total, Color color) {
        JPanel row = new JPanel(new BorderLayout(8, 6));
        row.setOpaque(false);

        JLabel dot = new JLabel("●");
        dot.setForeground(color);
        dot.setFont(AppTheme.BODY);
        JLabel name = new JLabel(label);
        name.setFont(AppTheme.BODY);
        name.setForeground(AppTheme.TEXT);
        JLabel value = new JLabel(count + " phòng");
        value.setFont(AppTheme.SUBHEADING);
        value.setForeground(AppTheme.TEXT);

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        left.setOpaque(false);
        left.add(dot);
        left.add(name);
        row.add(left, BorderLayout.WEST);
        row.add(value, BorderLayout.EAST);

        JProgressBar bar = new JProgressBar(0, (int) Math.max(total, 1));
        bar.setValue((int) count);
        bar.setForeground(color);
        bar.setBackground(new Color(241, 245, 249));
        bar.setBorderPainted(false);
        bar.setPreferredSize(new Dimension(0, 6));
        row.add(bar, BorderLayout.SOUTH);
        return row;
    }

    private void addStatusRow(String label, long count, long total, Color color) {
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.gridy = roomStatusList.getComponentCount();
        g.weightx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.anchor = GridBagConstraints.NORTHWEST;
        g.insets = new Insets(0, 0, 14, 0);
        roomStatusList.add(statusRow(label, count, total, color), g);
    }

    private void refreshHome() {
        try {
            List<Room> rooms = roomService.getRooms();
            long available = rooms.stream().filter(r -> "AVAILABLE".equalsIgnoreCase(r.status())).count();
            roomsCard.setValue(String.valueOf(rooms.size()));
            availableCard.setValue(String.valueOf(available));
            customersCard.setValue(String.valueOf(customerService.getCustomers().size()));
            bookingsCard.setValue(String.valueOf(bookingService.getBookings().size()));
            roomsCard.setHint(available + " phòng sẵn sàng");

            if (roomStatusList != null) {
                roomStatusList.removeAll();
                long total = Math.max(rooms.size(), 1);
                addStatusRow("Trống", count(rooms, "AVAILABLE"), total, AppTheme.SUCCESS);
                addStatusRow("Đang dùng", count(rooms, "OCCUPIED"), total, AppTheme.WARNING);
                addStatusRow("Bảo trì", count(rooms, "MAINTENANCE"), total, AppTheme.DANGER);
                addStatusRow("Ngưng dùng", count(rooms, "INACTIVE"), total, AppTheme.MUTED);
                GridBagConstraints filler = new GridBagConstraints();
                filler.gridx = 0;
                filler.gridy = roomStatusList.getComponentCount();
                filler.weighty = 1;
                filler.fill = GridBagConstraints.BOTH;
                roomStatusList.add(Box.createGlue(), filler);
                roomStatusList.revalidate();
                roomStatusList.repaint();
            }

            if (recentBookingsTable != null) {
                DefaultTableModel m = model("Mã đặt", "Khách hàng", "Nhận phòng", "Trả phòng", "Trạng thái", "Tổng tiền");
                List<Booking> latest = bookingService.getBookings();
                for (int i = 0; i < Math.min(latest.size(), 6); i++) {
                    Booking b = latest.get(i);
                    m.addRow(new Object[]{b.bookingCode(), b.customerName(), b.checkIn(), b.checkOut(), b.status(), b.total()});
                }
                recentBookingsTable.setModel(m);
                applyStatusColumn(recentBookingsTable, 4);
            }
        } catch (Exception e) {
            error(e);
        }
    }

    private long count(List<Room> rooms, String status) {
        return rooms.stream().filter(r -> status.equalsIgnoreCase(r.status())).count();
    }

    private JPanel roomsPanel() {
        roomsTable = new JTable();
        AppTheme.styleTable(roomsTable);
        roomsTable.getTableHeader().setReorderingAllowed(false);

        roomSearch = AppTheme.field(14);
        roomSearch.putClientProperty("JTextField.placeholderText", "Tìm số phòng / loại phòng");
        roomSearch.getDocument().addDocumentListener((SimpleDocListener) e -> renderRooms(roomsTable));
        roomStatusFilter = new JComboBox<>(new String[]{"Tất cả", "AVAILABLE", "OCCUPIED", "MAINTENANCE", "INACTIVE"});
        roomStatusFilter.setFont(AppTheme.BODY);
        roomStatusFilter.addActionListener(e -> renderRooms(roomsTable));
        JPanel filters = filterBar("Tìm phòng", roomSearch, "Trạng thái", roomStatusFilter);

        JButton refresh = AppTheme.ghostButton("Làm mới");
        refresh.addActionListener(e -> loadRooms(roomsTable));
        java.util.List<JButton> actions = new java.util.ArrayList<>();
        actions.add(refresh);
        if (isAdmin()) {
            JButton add = AppTheme.accentButton("Thêm phòng");
            JButton edit = AppTheme.ghostButton("Sửa phòng");
            JButton maintenance = AppTheme.ghostButton("Bảo trì / Mở lại");
            JButton types = AppTheme.ghostButton("Loại phòng");
            add.addActionListener(e -> addRoom(roomsTable));
            edit.addActionListener(e -> editRoom(roomsTable));
            maintenance.addActionListener(e -> toggleRoomStatus(roomsTable));
            types.addActionListener(e -> manageRoomTypes());
            actions.add(add);
            actions.add(edit);
            actions.add(maintenance);
            actions.add(types);
        }
        JPanel panel = tablePage("Danh sách phòng",
                isAdmin() ? "Quản trị có thể thêm, sửa phòng và đổi trạng thái." : "Theo dõi loại phòng, giá và trạng thái.",
                roomsTable, filters, actions.toArray(JButton[]::new));
        loadRooms(roomsTable);
        return panel;
    }

    private void loadRooms(JTable table) {
        try {
            allRooms.clear();
            allRooms.addAll(roomService.getRooms());
            renderRooms(table);
        } catch (Exception e) {
            error(e);
        }
    }

    private void renderRooms(JTable table) {
        if (table == null) return;
        String q = searchText(roomSearch);
        String status = comboValue(roomStatusFilter);
        DefaultTableModel m = model("ID", "Số phòng", "Loại phòng", "Giá/đêm", "Tầng", "Trạng thái");
        for (Room r : allRooms) {
            boolean hit = q.isEmpty() || contains(q, r.roomNumber(), r.typeName(), r.status());
            boolean stOk = "Tất cả".equals(status) || status.equalsIgnoreCase(r.status());
            if (hit && stOk) {
                m.addRow(new Object[]{r.id(), r.roomNumber(), r.typeName(), r.pricePerNight(), r.floor(), r.status()});
            }
        }
        table.setModel(m);
        applyStatusColumn(table, 5);
    }

    private JPanel customersPanel() {
        customersTable = new JTable();
        AppTheme.styleTable(customersTable);

        customerSearch = AppTheme.field(18);
        customerSearch.putClientProperty("JTextField.placeholderText", "Tìm theo tên / điện thoại / email / CCCD");
        customerSearch.getDocument().addDocumentListener((SimpleDocListener) e -> renderCustomers(customersTable));
        JPanel filters = filterBar("Tìm khách hàng", customerSearch, null, null);

        JButton refresh = AppTheme.ghostButton("Làm mới");
        JButton add = AppTheme.accentButton("Thêm khách hàng");
        JButton edit = AppTheme.ghostButton("Cập nhật");
        refresh.addActionListener(e -> loadCustomers(customersTable));
        add.addActionListener(e -> addCustomer(customersTable));
        edit.addActionListener(e -> editCustomer(customersTable));
        JPanel panel = tablePage("Hồ sơ khách hàng", "Thêm, tra cứu và cập nhật thông tin khách lưu trú.",
                customersTable, filters, refresh, add, edit);
        loadCustomers(customersTable);
        return panel;
    }

    private void loadCustomers(JTable table) {
        try {
            allCustomers.clear();
            allCustomers.addAll(customerService.getCustomers());
            renderCustomers(table);
        } catch (Exception e) {
            error(e);
        }
    }

    private void renderCustomers(JTable table) {
        if (table == null) return;
        String q = searchText(customerSearch);
        DefaultTableModel m = model("ID", "Họ tên", "Điện thoại", "Email", "CCCD", "Địa chỉ");
        for (Customer c : allCustomers) {
            if (!q.isEmpty() && !contains(q, c.fullName(), c.phone(), c.email(), c.identityNumber(), c.address())) continue;
            m.addRow(new Object[]{c.id(), c.fullName(), c.phone(), c.email(), c.identityNumber(), c.address()});
        }
        table.setModel(m);
    }

    private void addCustomer(JTable table) {
        JTextField name = AppTheme.field(20);
        JTextField phone = AppTheme.field(20);
        JTextField email = AppTheme.field(20);
        JTextField identity = AppTheme.field(20);
        JTextField address = AppTheme.field(20);
        JPanel form = formPanel(
                "Họ tên", name,
                "Điện thoại", phone,
                "Email", email,
                "CCCD", identity,
                "Địa chỉ", address
        );
        if (JOptionPane.showConfirmDialog(this, form, "Thêm khách hàng", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE)
                == JOptionPane.OK_OPTION) {
            try {
                customerService.add(name.getText(), phone.getText(), email.getText(), identity.getText(), address.getText());
                loadCustomers(table);
                refreshHome();
            } catch (Exception e) {
                error(e);
            }
        }
    }

    private void editCustomer(JTable table) {
        int row = selectedModelRow(table);
        if (row < 0) return;
        JTextField name = AppTheme.field(20);
        JTextField phone = AppTheme.field(20);
        JTextField email = AppTheme.field(20);
        JTextField identity = AppTheme.field(20);
        JTextField address = AppTheme.field(20);
        name.setText(String.valueOf(table.getModel().getValueAt(row, 1)));
        phone.setText(nullToEmpty(table.getModel().getValueAt(row, 2)));
        email.setText(nullToEmpty(table.getModel().getValueAt(row, 3)));
        identity.setText(nullToEmpty(table.getModel().getValueAt(row, 4)));
        address.setText(nullToEmpty(table.getModel().getValueAt(row, 5)));
        JPanel form = formPanel("Họ tên", name, "Điện thoại", phone, "Email", email, "CCCD", identity, "Địa chỉ", address);
        if (JOptionPane.showConfirmDialog(this, form, "Cập nhật khách hàng", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE)
                == JOptionPane.OK_OPTION) {
            try {
                long id = Long.parseLong(table.getModel().getValueAt(row, 0).toString());
                customerService.update(id, name.getText(), phone.getText(), email.getText(), identity.getText(), address.getText());
                loadCustomers(table);
            } catch (Exception e) {
                error(e);
            }
        }
    }

    private void addRoom(JTable table) {
        try {
            List<RoomType> types = roomService.getTypes();
            if (types.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Chưa có loại phòng.", "Thiếu dữ liệu", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            String[] options = types.stream()
                    .map(t -> t.id() + " - " + t.typeName() + " (" + t.pricePerNight() + ")")
                    .toArray(String[]::new);
            JTextField number = AppTheme.field(12);
            JComboBox<String> type = new JComboBox<>(options);
            JTextField floor = AppTheme.field(8);
            floor.setText("1");
            type.setFont(AppTheme.BODY);
            JPanel form = formPanel("Số phòng", number, "Loại phòng", type, "Tầng", floor);
            if (JOptionPane.showConfirmDialog(this, form, "Thêm phòng", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE)
                    == JOptionPane.OK_OPTION) {
                long typeId = Long.parseLong(((String) type.getSelectedItem()).split(" - ")[0]);
                roomService.add(number.getText(), typeId, Integer.parseInt(floor.getText().trim()));
                loadRooms(table);
                refreshHome();
            }
        } catch (Exception e) {
            error(e);
        }
    }

    private void toggleRoomStatus(JTable table) {
        int row = selectedModelRow(table);
        if (row < 0) return;
        try {
            long id = Long.parseLong(table.getModel().getValueAt(row, 0).toString());
            String status = String.valueOf(table.getModel().getValueAt(row, 5));
            String next = "MAINTENANCE".equalsIgnoreCase(status) ? "AVAILABLE" : "MAINTENANCE";
            roomService.setStatus(id, next);
            loadRooms(table);
            refreshHome();
        } catch (Exception e) {
            error(e);
        }
    }

    private JPanel bookingsPanel() {
        bookingsTable = new JTable();
        AppTheme.styleTable(bookingsTable);

        bookingSearch = AppTheme.field(14);
        bookingSearch.putClientProperty("JTextField.placeholderText", "Tìm mã đặt / khách hàng / điện thoại");
        bookingSearch.getDocument().addDocumentListener((SimpleDocListener) e -> renderBookings(bookingsTable));
        bookingStatusFilter = new JComboBox<>(new String[]{"Tất cả", "PENDING", "CONFIRMED", "CHECKED_IN", "CHECKED_OUT", "CANCELLED"});
        bookingStatusFilter.setFont(AppTheme.BODY);
        bookingStatusFilter.addActionListener(e -> renderBookings(bookingsTable));
        JPanel filters = filterBar("Tìm đặt phòng", bookingSearch, "Trạng thái", bookingStatusFilter);

        JButton refresh = AppTheme.ghostButton("Làm mới");
        JButton add = AppTheme.accentButton("Tạo đặt phòng");
        JButton details = AppTheme.ghostButton("Chi tiết phòng");
        JButton split = AppTheme.ghostButton("Tách phòng");
        JButton merge = AppTheme.ghostButton("Ghép đơn");
        JButton checkIn = AppTheme.ghostButton("Nhận phòng");
        JButton checkOut = AppTheme.ghostButton("Trả phòng");
        JButton pay = AppTheme.accentButton("Thanh toán");
        JButton invoice = AppTheme.ghostButton("Hóa đơn");
        JButton cancel = AppTheme.dangerButton("Hủy");
        refresh.addActionListener(e -> loadBookings(bookingsTable));
        add.addActionListener(e -> createBooking(bookingsTable));
        details.addActionListener(e -> showBookingDetails(bookingsTable));
        split.addActionListener(e -> splitBooking(bookingsTable));
        merge.addActionListener(e -> mergeBookings(bookingsTable));
        checkIn.addActionListener(e -> changeBooking(bookingsTable, "checkin"));
        checkOut.addActionListener(e -> changeBooking(bookingsTable, "checkout"));
        pay.addActionListener(e -> payBooking(bookingsTable));
        invoice.addActionListener(e -> showInvoice(bookingsTable));
        cancel.addActionListener(e -> cancelBooking(bookingsTable));
        JPanel panel = tablePage("Lịch đặt phòng",
                "Đặt nhiều phòng (đoàn khách), tách/ghép đơn. Cuối tuần +10%, cao điểm theo mùa và mã khuyến mãi.",
                bookingsTable, filters, refresh, add, details, split, merge, checkIn, checkOut, pay, invoice, cancel);
        loadBookings(bookingsTable);
        return panel;
    }

    private void loadBookings(JTable table) {
        try {
            allBookings.clear();
            allBookings.addAll(bookingService.getBookings());
            renderBookings(table);
        } catch (Exception e) {
            error(e);
        }
    }

    private void renderBookings(JTable table) {
        if (table == null) return;
        String q = searchText(bookingSearch);
        String status = comboValue(bookingStatusFilter);
        DefaultTableModel m = model("ID", "Mã đặt", "Khách hàng", "Điện thoại", "Nhận phòng", "Trả phòng", "Trạng thái", "Thanh toán", "Tổng tiền");
        for (Booking b : allBookings) {
            boolean hit = q.isEmpty() || contains(q, b.bookingCode(), b.customerName(), b.phone(), b.status());
            boolean stOk = "Tất cả".equals(status) || status.equalsIgnoreCase(b.status());
            if (hit && stOk) {
                m.addRow(new Object[]{b.id(), b.bookingCode(), b.customerName(), b.phone(), b.checkIn(), b.checkOut(), b.status(), b.paymentStatus(), b.total()});
            }
        }
        table.setModel(m);
        applyStatusColumn(table, 6);
        applyStatusColumn(table, 7);
    }

    private void createBooking(JTable table) {
        try {
            List<Customer> customers = customerService.getCustomers();
            List<Room> rooms = roomService.getRooms();
            if (customers.isEmpty() || rooms.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Cần có khách hàng và phòng trước.", "Thiếu dữ liệu", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            String[] cs = customers.stream().map(c -> c.id() + " - " + c.fullName() + " (" + c.phone() + ")").toArray(String[]::new);
            // Cho phép đặt cả phòng đang có khách (OCCUPIED) nếu khoảng ngày nằm ở tương lai.
            List<Room> bookable = rooms.stream()
                    .filter(r -> "AVAILABLE".equalsIgnoreCase(r.status()) || "OCCUPIED".equalsIgnoreCase(r.status()))
                    .toList();
            if (bookable.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Không có phòng khả dụng.", "Hết phòng", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            JComboBox<String> customer = new JComboBox<>(cs);
            customer.setFont(AppTheme.BODY);
            DefaultListModel<String> roomModel = new DefaultListModel<>();
            for (Room r : bookable) {
                roomModel.addElement(r.id() + " - " + r.roomNumber() + " / " + r.typeName() + " [" + r.status() + "]");
            }
            JList<String> roomList = new JList<>(roomModel);
            roomList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
            roomList.setFont(AppTheme.BODY);
            roomList.setVisibleRowCount(6);
            JScrollPane roomScroll = new JScrollPane(roomList);
            roomScroll.setPreferredSize(new Dimension(340, 132));
            roomScroll.setBorder(BorderFactory.createLineBorder(AppTheme.BORDER, 1, true));

            JTextField in = AppTheme.field(16);
            JTextField out = AppTheme.field(16);
            in.setText(LocalDate.now().toString());
            out.setText(LocalDate.now().plusDays(1).toString());
            JTextField promo = AppTheme.field(16);
            promo.putClientProperty("JTextField.placeholderText", "VD: WELCOME5, SUMMER10, GROUP15");
            JLabel estimate = AppTheme.muted("Chọn phòng và ngày để xem giá dự kiến.");
            estimate.setForeground(AppTheme.INFO);

            Runnable recompute = () -> {
                try {
                    List<Long> ids = selectedRoomIds(roomList, bookable);
                    if (ids.isEmpty()) {
                        estimate.setText("Chọn ít nhất một phòng.");
                        return;
                    }
                    LocalDate ci = LocalDate.parse(in.getText().trim());
                    LocalDate co = LocalDate.parse(out.getText().trim());
                    long nights = java.time.temporal.ChronoUnit.DAYS.between(ci, co);
                    if (nights <= 0) {
                        estimate.setText("Ngày trả phải sau ngày nhận.");
                        return;
                    }
                    String code = promo.getText().trim();
                    BigDecimal total = bookingService.estimate(ids, ci, co, code.isEmpty() ? null : code);
                    estimate.setText("Dự kiến: " + ids.size() + " phòng, " + nights + " đêm — " + total);
                } catch (Exception ex) {
                    estimate.setText("Chưa xác định được giá dự kiến.");
                }
            };
            in.getDocument().addDocumentListener((SimpleDocListener) e -> recompute.run());
            out.getDocument().addDocumentListener((SimpleDocListener) e -> recompute.run());
            promo.getDocument().addDocumentListener((SimpleDocListener) e -> recompute.run());
            roomList.addListSelectionListener(e -> {
                if (!e.getValueIsAdjusting()) recompute.run();
            });

            JPanel form = formPanel(
                    "Khách hàng", customer,
                    "Phòng (giữ Ctrl để chọn nhiều phòng)", roomScroll,
                    "Ngày nhận (YYYY-MM-DD)", in,
                    "Ngày trả (YYYY-MM-DD)", out,
                    "Mã khuyến mãi (nếu có)", promo,
                    "Giá dự kiến", estimate
            );
            if (JOptionPane.showConfirmDialog(this, form, "Tạo đặt phòng (đoàn khách)",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION) {
                List<Long> ids = selectedRoomIds(roomList, bookable);
                if (ids.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Vui lòng chọn ít nhất một phòng.", "Thiếu phòng", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                long cid = Long.parseLong(((String) customer.getSelectedItem()).split(" - ")[0]);
                String code = promo.getText().trim();
                bookingService.book(cid, ids, LocalDate.parse(in.getText().trim()), LocalDate.parse(out.getText().trim()),
                        session.id(), code.isEmpty() ? null : code);
                loadBookings(table);
                loadRooms(roomsTable);
                refreshHome();
                JOptionPane.showMessageDialog(this, "Tạo đặt phòng " + ids.size() + " phòng thành công.", "Hoàn tất", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            error(e);
        }
    }

    private List<Long> selectedRoomIds(JList<String> list, List<Room> bookable) {
        List<Long> ids = new java.util.ArrayList<>();
        for (int index : list.getSelectedIndices()) {
            ids.add(bookable.get(index).id());
        }
        return ids;
    }

    private void showBookingDetails(JTable table) {
        Long id = selectedBookingId(table);
        if (id == null) return;
        try {
            List<BookingDetail> details = bookingService.details(id);
            DefaultTableModel m = model("Phòng", "Loại phòng", "Giá/đêm", "Số đêm", "Thành tiền");
            for (BookingDetail d : details) {
                m.addRow(new Object[]{d.roomNumber(), d.typeName(), d.pricePerNight(), d.nights(), d.lineTotal()});
            }
            JTable detailTable = new JTable(m);
            AppTheme.styleTable(detailTable);
            JScrollPane scroll = AppTheme.wrapTable(detailTable);
            scroll.setPreferredSize(new Dimension(560, 240));
            JLabel head = AppTheme.muted("Đơn #" + id + " gồm " + details.size() + " phòng.");
            JPanel panel = new JPanel(new BorderLayout(8, 8));
            panel.setOpaque(false);
            panel.add(head, BorderLayout.NORTH);
            panel.add(scroll, BorderLayout.CENTER);
            JOptionPane.showMessageDialog(this, panel, "Chi tiết phòng của đơn", JOptionPane.PLAIN_MESSAGE);
        } catch (Exception e) {
            error(e);
        }
    }

    private void splitBooking(JTable table) {
        Long id = selectedBookingId(table);
        if (id == null) return;
        try {
            List<BookingDetail> details = bookingService.details(id);
            if (details.size() <= 1) {
                JOptionPane.showMessageDialog(this, "Đơn chỉ có một phòng, không cần tách.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            DefaultTableModel m = model("ID dòng", "Phòng", "Loại phòng", "Số đêm", "Thành tiền");
            for (BookingDetail d : details) {
                m.addRow(new Object[]{d.id(), d.roomNumber(), d.typeName(), d.nights(), d.lineTotal()});
            }
            JTable pick = new JTable(m);
            AppTheme.styleTable(pick);
            pick.setRowSelectionInterval(0, 0);
            JScrollPane scroll = AppTheme.wrapTable(pick);
            scroll.setPreferredSize(new Dimension(560, 220));
            JLabel hint = AppTheme.muted("Chọn phòng muốn tách sang đơn mới.");
            JPanel panel = new JPanel(new BorderLayout(8, 8));
            panel.setOpaque(false);
            panel.add(hint, BorderLayout.NORTH);
            panel.add(scroll, BorderLayout.CENTER);
            if (JOptionPane.showConfirmDialog(this, panel, "Tách phòng khỏi đơn #" + id,
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) return;
            int row = pick.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Chưa chọn phòng để tách.", "Chưa chọn", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            long detailId = Long.parseLong(pick.getModel().getValueAt(pick.convertRowIndexToModel(row), 0).toString());
            long newId = bookingService.splitRoom(id, detailId);
            reloadBookings(table);
            JOptionPane.showMessageDialog(this, "Đã tách sang đơn mới #" + newId + ".", "Hoàn tất", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            error(e);
        }
    }

    private void mergeBookings(JTable table) {
        Long sourceId = selectedBookingId(table);
        if (sourceId == null) return;
        try {
            Booking source = bookingService.findById(sourceId);
            List<Booking> candidates = allBookings.stream()
                    .filter(b -> b.id() != sourceId)
                    .filter(b -> b.customerName() != null && b.customerName().equals(source.customerName()))
                    .filter(b -> b.checkIn().equals(source.checkIn()) && b.checkOut().equals(source.checkOut()))
                    .toList();
            if (candidates.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Không tìm thấy đơn nào cùng khách và cùng khoảng ngày để ghép.",
                        "Không thể ghép", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            String[] options = candidates.stream()
                    .map(b -> b.id() + " - " + b.bookingCode() + " [" + AppTheme.statusLabel(b.status()) + "]")
                    .toArray(String[]::new);
            JComboBox<String> target = new JComboBox<>(options);
            target.setFont(AppTheme.BODY);
            JLabel hint = AppTheme.muted("Gộp toàn bộ phòng của đơn #" + sourceId + " vào đơn được chọn.");
            JPanel form = formPanel("Đơn đích", target, "Ghi chú", hint);
            if (JOptionPane.showConfirmDialog(this, form, "Ghép đơn #" + sourceId,
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) return;
            long targetId = Long.parseLong(((String) target.getSelectedItem()).split(" - ")[0]);
            bookingService.merge(sourceId, targetId);
            reloadBookings(table);
            JOptionPane.showMessageDialog(this, "Đã gộp đơn #" + sourceId + " vào đơn #" + targetId + ".",
                    "Hoàn tất", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            error(e);
        }
    }

    private void changeBooking(JTable table, String action) {
        Long id = selectedBookingId(table);
        if (id == null) return;
        try {
            if ("checkin".equals(action)) bookingService.checkIn(id);
            else bookingService.checkOut(id);
            reloadBookings(table);
        } catch (Exception e) {
            error(e);
        }
    }

    private void payBooking(JTable table) {
        Long id = selectedBookingId(table);
        if (id == null) return;
        try {
            BigDecimal total = null;
            for (Booking b : allBookings) {
                if (b.id() == id) { total = b.total(); break; }
            }
            BigDecimal remaining = bookingService.remaining(id);
            if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
                JOptionPane.showMessageDialog(this, "Đơn này đã thanh toán đủ.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            JComboBox<String> method = new JComboBox<>(new String[]{"Tiền mặt", "Thẻ", "Chuyển khoản"});
            method.setFont(AppTheme.BODY);
            JTextField amount = AppTheme.field(16);
            amount.setText(remaining.toPlainString());
            JLabel info = AppTheme.muted("Tổng đơn: " + total + " — Còn lại: " + remaining);
            info.setForeground(AppTheme.WARNING);
            JPanel form = formPanel(
                    "Phương thức", method,
                    "Số tiền thu", amount,
                    "Tình trạng", info
            );
            if (JOptionPane.showConfirmDialog(this, form, "Thanh toán (có thể thu từng phần)",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) return;
            String selected = (String) method.getSelectedItem();
            String code = switch (selected) {
                case "Thẻ" -> "CARD";
                case "Chuyển khoản" -> "BANK_TRANSFER";
                default -> "CASH";
            };
            PaymentService.SettleResult result = bookingService.pay(id, code, new BigDecimal(amount.getText().trim()));
            reloadBookings(table);
            if (result.fullyPaid()) {
                showInvoiceDialog(result.invoice());
            } else {
                JOptionPane.showMessageDialog(this,
                        "Đã ghi nhận " + result.paid() + ". Còn lại " + result.remaining() + ".",
                        "Thanh toán một phần", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            error(e);
        }
    }

    private void showInvoice(JTable table) {
        Long id = selectedBookingId(table);
        if (id == null) return;
        try {
            showInvoiceDialog(bookingService.invoice(id));
        } catch (Exception e) {
            error(e);
        }
    }

    private void showInvoiceDialog(Invoice invoice) {
        JLabel body = new JLabel("<html><div style='width:280px'>"
                + "<b>Hóa đơn " + invoice.invoiceCode() + "</b><br><br>"
                + "Mã đặt: " + invoice.bookingCode() + "<br>"
                + "Khách hàng: " + invoice.customerName() + "<br>"
                + "Tổng tiền: " + invoice.totalAmount() + "<br>"
                + "Phát hành: " + invoice.issuedAt()
                + "</div></html>");
        JOptionPane.showMessageDialog(this, body, "Hóa đơn", JOptionPane.INFORMATION_MESSAGE);
    }

    private void cancelBooking(JTable table) {
        Long id = selectedBookingId(table);
        if (id == null) return;
        if (JOptionPane.showConfirmDialog(this, "Bạn chắc chắn muốn hủy đặt phòng #" + id + "?", "Xác nhận",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            try {
                bookingService.cancel(id);
                reloadBookings(table);
            } catch (Exception e) {
                error(e);
            }
        }
    }

    private void reloadBookings(JTable table) {
        loadBookings(table);
        if (roomsTable != null) loadRooms(roomsTable);
        refreshHome();
    }

    private JPanel invoicesPanel() {
        invoicesTable = new JTable();
        AppTheme.styleTable(invoicesTable);
        invoicesTable.getTableHeader().setReorderingAllowed(false);

        invoiceSearch = AppTheme.field(18);
        invoiceSearch.putClientProperty("JTextField.placeholderText", "Tìm mã hóa đơn / mã đặt / khách hàng");
        invoiceSearch.getDocument().addDocumentListener((SimpleDocListener) e -> renderInvoices(invoicesTable));
        JPanel filters = filterBar("Tìm hóa đơn", invoiceSearch, null, null);

        JButton refresh = AppTheme.ghostButton("Làm mới");
        refresh.addActionListener(e -> loadInvoices(invoicesTable));
        JPanel panel = tablePage("Hóa đơn & doanh thu",
                "Danh sách hóa đơn đã phát hành và tổng doanh thu đã thu.", invoicesTable, filters, refresh);

        revenueLabel = new JLabel("Tổng doanh thu đã thu: —");
        revenueLabel.setFont(AppTheme.SUBHEADING);
        revenueLabel.setForeground(AppTheme.SUCCESS);
        JPanel revenueBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 28, 0));
        revenueBar.setOpaque(false);
        revenueBar.add(revenueLabel);
        panel.add(revenueBar, BorderLayout.SOUTH);

        loadInvoices(invoicesTable);
        return panel;
    }

    private void loadInvoices(JTable table) {
        try {
            allInvoices.clear();
            allInvoices.addAll(paymentService.getInvoices());
            renderInvoices(table);
            if (revenueLabel != null) {
                revenueLabel.setText("Tổng doanh thu đã thu: " + paymentService.getRevenue());
            }
        } catch (Exception e) {
            error(e);
        }
    }

    private void renderInvoices(JTable table) {
        if (table == null) return;
        String q = searchText(invoiceSearch);
        DefaultTableModel m = model("ID", "Mã hóa đơn", "Mã đặt", "Khách hàng", "Tổng tiền", "Phát hành");
        for (Invoice inv : allInvoices) {
            if (!q.isEmpty() && !contains(q, inv.invoiceCode(), inv.bookingCode(), inv.customerName())) continue;
            m.addRow(new Object[]{inv.id(), inv.invoiceCode(), inv.bookingCode(),
                    inv.customerName(), inv.totalAmount(), inv.issuedAt()});
        }
        table.setModel(m);
    }

    private JPanel reportsPanel() {
        JPanel p = new JPanel(new BorderLayout(12, 12));
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(16, 19, 19, 19));

        RoundedPanel card = new RoundedPanel(18, AppTheme.CARD, true);
        card.setLayout(new BorderLayout(0, 12));
        card.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        reportFrom = AppTheme.field(12);
        reportTo = AppTheme.field(12);
        reportFrom.setText(LocalDate.now().withDayOfMonth(1).toString());
        reportTo.setText(LocalDate.now().toString());
        JButton byDay = AppTheme.accentButton("Theo ngày");
        JButton byMonth = AppTheme.ghostButton("Theo tháng");
        JButton byType = AppTheme.ghostButton("Theo loại phòng");
        byDay.addActionListener(e -> refreshReport());
        byMonth.addActionListener(e -> showMonthlyReport());
        byType.addActionListener(e -> showRoomTypeReport());

        JPanel filters = filterBar("Từ ngày", reportFrom, "Đến ngày", reportTo);
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        btns.setOpaque(false);
        btns.add(byDay);
        btns.add(byMonth);
        btns.add(byType);

        JPanel top = new JPanel(new BorderLayout(0, 8));
        top.setOpaque(false);
        JPanel titles = new JPanel();
        titles.setOpaque(false);
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
        JLabel t = new JLabel("Biểu đồ doanh thu & công suất");
        t.setFont(AppTheme.SUBHEADING);
        t.setForeground(AppTheme.TEXT);
        titles.add(t);
        titles.add(AppTheme.muted("Doanh thu được tính từ các khoản đã thu; công suất = số đêm phòng đã bán / tổng số đêm phòng."));
        JPanel bottomBar = new JPanel(new BorderLayout(0, 6));
        bottomBar.setOpaque(false);
        bottomBar.add(filters, BorderLayout.NORTH);
        bottomBar.add(btns, BorderLayout.CENTER);
        top.add(titles, BorderLayout.NORTH);
        top.add(bottomBar, BorderLayout.SOUTH);
        card.add(top, BorderLayout.NORTH);

        reportChart = new BarChartPanel();
        JPanel chartWrap = new JPanel(new BorderLayout());
        chartWrap.setOpaque(false);
        chartWrap.setBorder(BorderFactory.createLineBorder(AppTheme.BORDER, 1, true));
        chartWrap.add(reportChart, BorderLayout.CENTER);
        card.add(chartWrap, BorderLayout.CENTER);

        reportSummaryLabel = new JLabel("—");
        reportSummaryLabel.setFont(AppTheme.SUBHEADING);
        reportSummaryLabel.setForeground(AppTheme.SUCCESS);
        JPanel summaryBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        summaryBar.setOpaque(false);
        summaryBar.add(reportSummaryLabel);
        card.add(summaryBar, BorderLayout.SOUTH);

        p.add(card, BorderLayout.CENTER);
        refreshReport();
        return p;
    }

    private void refreshReport() {
        if (reportChart == null) return;
        try {
            LocalDate from = LocalDate.parse(reportFrom.getText().trim());
            LocalDate to = LocalDate.parse(reportTo.getText().trim());
            List<ChartPoint> points = reportService.revenueByDay(from, to);
            reportChart.setData("Doanh thu theo ngày", "đơn vị: VND", points, AppTheme.GOLD);
            ReportSummary s = reportService.summary(from, to);
            reportSummaryLabel.setText(String.format(Locale.ROOT,
                    "Tổng doanh thu: %s VND   |   Số đơn: %d   |   Công suất phòng: %.1f%%",
                    s.totalRevenue(), s.bookingCount(), s.occupancyRate() * 100));
        } catch (Exception e) {
            error(e);
        }
    }

    private void showMonthlyReport() {
        if (reportChart == null) return;
        try {
            LocalDate from = LocalDate.parse(reportFrom.getText().trim());
            LocalDate to = LocalDate.parse(reportTo.getText().trim());
            List<ChartPoint> points = reportService.revenueByMonth(from, to);
            reportChart.setData("Doanh thu theo tháng", "đơn vị: VND", points, AppTheme.INFO);
        } catch (Exception e) {
            error(e);
        }
    }

    private void showRoomTypeReport() {
        if (reportChart == null) return;
        try {
            List<ChartPoint> points = reportService.revenueByRoomType();
            reportChart.setData("Doanh thu theo loại phòng", "đơn vị: VND (dự kiến)", points, AppTheme.SUCCESS);
        } catch (Exception e) {
            error(e);
        }
    }

    private void manageRoomTypes() {
        DefaultTableModel m = model("ID", "Loại phòng", "Mô tả", "Giá/đêm", "Sức chứa", "Trạng thái");
        JTable table = new JTable(m);
        AppTheme.styleTable(table);
        Runnable reload = () -> {
            try {
                m.setRowCount(0);
                for (RoomType t : roomService.getAllTypes()) {
                    m.addRow(new Object[]{t.id(), t.typeName(), t.description(), t.pricePerNight(),
                            t.capacity(), t.active() ? "Đang dùng" : "Ngưng"});
                }
            } catch (Exception e) {
                error(e);
            }
        };
        reload.run();

        JButton add = AppTheme.accentButton("Thêm loại");
        JButton edit = AppTheme.ghostButton("Sửa");
        JButton price = AppTheme.ghostButton("Cập nhật giá");
        JButton toggle = AppTheme.ghostButton("Ngưng dùng / Kích hoạt");
        add.addActionListener(e -> {
            if (addRoomType()) reload.run();
        });
        edit.addActionListener(e -> {
            if (editRoomType(table)) reload.run();
        });
        price.addActionListener(e -> {
            if (updateTypePrice(table)) reload.run();
        });
        toggle.addActionListener(e -> {
            if (toggleTypeActive(table)) reload.run();
        });

        JScrollPane scroll = AppTheme.wrapTable(table);
        scroll.setPreferredSize(new Dimension(660, 300));
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(false);
        panel.add(scroll, BorderLayout.CENTER);
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        btns.setOpaque(false);
        btns.add(add);
        btns.add(edit);
        btns.add(price);
        btns.add(toggle);
        panel.add(btns, BorderLayout.SOUTH);
        JOptionPane.showMessageDialog(this, panel, "Quản lý loại phòng", JOptionPane.PLAIN_MESSAGE);
    }

    private boolean addRoomType() {
        JTextField name = AppTheme.field(18);
        JTextField desc = AppTheme.field(18);
        JTextField price = AppTheme.field(18);
        JTextField capacity = AppTheme.field(18);
        capacity.setText("2");
        JPanel form = formPanel(
                "Tên loại phòng", name,
                "Mô tả", desc,
                "Giá/đêm", price,
                "Sức chứa", capacity
        );
        if (JOptionPane.showConfirmDialog(this, form, "Thêm loại phòng", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE)
                != JOptionPane.OK_OPTION) {
            return false;
        }
        try {
            roomService.addType(name.getText(), desc.getText(),
                    new BigDecimal(price.getText().trim()), Integer.parseInt(capacity.getText().trim()));
            return true;
        } catch (Exception e) {
            error(e);
            return false;
        }
    }

    private boolean editRoomType(JTable table) {
        int row = selectedModelRow(table);
        if (row < 0) return false;
        long id = Long.parseLong(table.getModel().getValueAt(row, 0).toString());
        JTextField name = AppTheme.field(18);
        JTextField desc = AppTheme.field(18);
        JTextField price = AppTheme.field(18);
        JTextField capacity = AppTheme.field(18);
        name.setText(String.valueOf(table.getModel().getValueAt(row, 1)));
        desc.setText(nullToEmpty(table.getModel().getValueAt(row, 2)));
        price.setText(String.valueOf(table.getModel().getValueAt(row, 3)));
        capacity.setText(String.valueOf(table.getModel().getValueAt(row, 4)));
        JPanel form = formPanel(
                "Tên loại phòng", name,
                "Mô tả", desc,
                "Giá/đêm", price,
                "Sức chứa", capacity
        );
        if (JOptionPane.showConfirmDialog(this, form, "Sửa loại phòng", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE)
                != JOptionPane.OK_OPTION) {
            return false;
        }
        try {
            roomService.updateType(id, name.getText(), desc.getText(),
                    new BigDecimal(price.getText().trim()), Integer.parseInt(capacity.getText().trim()));
            return true;
        } catch (Exception e) {
            error(e);
            return false;
        }
    }

    private boolean updateTypePrice(JTable table) {
        int row = selectedModelRow(table);
        if (row < 0) return false;
        long id = Long.parseLong(table.getModel().getValueAt(row, 0).toString());
        JTextField price = AppTheme.field(18);
        price.setText(String.valueOf(table.getModel().getValueAt(row, 3)));
        JPanel form = formPanel("Giá/đêm mới", price);
        if (JOptionPane.showConfirmDialog(this, form, "Cập nhật giá loại phòng", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE)
                != JOptionPane.OK_OPTION) {
            return false;
        }
        try {
            roomService.updateTypePrice(id, new BigDecimal(price.getText().trim()));
            return true;
        } catch (Exception e) {
            error(e);
            return false;
        }
    }

    private boolean toggleTypeActive(JTable table) {
        int row = selectedModelRow(table);
        if (row < 0) return false;
        long id = Long.parseLong(table.getModel().getValueAt(row, 0).toString());
        boolean active = "Đang dùng".equals(String.valueOf(table.getModel().getValueAt(row, 5)));
        String action = active ? "ngưng dùng" : "kích hoạt lại";
        if (JOptionPane.showConfirmDialog(this, "Bạn muốn " + action + " loại phòng #" + id + "?", "Xác nhận",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return false;
        }
        try {
            roomService.setTypeActive(id, !active);
            return true;
        } catch (Exception e) {
            error(e);
            return false;
        }
    }

    private void editRoom(JTable table) {
        int row = selectedModelRow(table);
        if (row < 0) return;
        try {
            long id = Long.parseLong(table.getModel().getValueAt(row, 0).toString());
            List<RoomType> types = roomService.getAllTypes();
            if (types.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Chưa có loại phòng.", "Thiếu dữ liệu", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            String[] options = types.stream()
                    .map(t -> t.id() + " - " + t.typeName() + " (" + t.pricePerNight() + ")" + (t.active() ? "" : " [ngưng]"))
                    .toArray(String[]::new);
            JComboBox<String> type = new JComboBox<>(options);
            type.setFont(AppTheme.BODY);
            JTextField number = AppTheme.field(12);
            number.setText(String.valueOf(table.getModel().getValueAt(row, 1)));
            JTextField floor = AppTheme.field(8);
            floor.setText(String.valueOf(table.getModel().getValueAt(row, 4)));
            // Chọn sẵn loại phòng hiện tại theo tên.
            String currentType = String.valueOf(table.getModel().getValueAt(row, 2));
            for (int i = 0; i < types.size(); i++) {
                if (types.get(i).typeName().equals(currentType)) {
                    type.setSelectedIndex(i);
                    break;
                }
            }
            JPanel form = formPanel("Số phòng", number, "Loại phòng", type, "Tầng", floor);
            if (JOptionPane.showConfirmDialog(this, form, "Sửa phòng", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE)
                    != JOptionPane.OK_OPTION) return;
            long typeId = Long.parseLong(((String) type.getSelectedItem()).split(" - ")[0]);
            roomService.update(id, number.getText(), typeId, Integer.parseInt(floor.getText().trim()));
            loadRooms(table);
            refreshHome();
        } catch (Exception e) {
            error(e);
        }
    }

    private Long selectedBookingId(JTable table) {
        int row = selectedModelRow(table);
        if (row < 0) return null;
        return Long.parseLong(table.getModel().getValueAt(row, 0).toString());
    }

    private int selectedModelRow(JTable table) {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Chọn một dòng trên bảng.", "Chưa chọn dòng", JOptionPane.INFORMATION_MESSAGE);
            return -1;
        }
        return table.convertRowIndexToModel(row);
    }

    private boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(session.role());
    }

    private String nullToEmpty(Object value) {
        return value == null ? "" : value.toString();
    }

    private JPanel tablePage(String title, String subtitle, JTable table, JButton... actions) {
        return tablePage(title, subtitle, table, null, actions);
    }

    private JPanel tablePage(String title, String subtitle, JTable table, JPanel filters, JButton... actions) {
        JPanel p = new JPanel(new BorderLayout(12, 12));
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(16, 19, 19, 19));

        RoundedPanel card = new RoundedPanel(18, AppTheme.CARD, true);
        card.setLayout(new BorderLayout(0, 12));
        card.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel top = new JPanel(new BorderLayout(0, 8));
        top.setOpaque(false);
        JPanel titles = new JPanel();
        titles.setOpaque(false);
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
        JLabel t = new JLabel(title);
        t.setFont(AppTheme.SUBHEADING);
        t.setForeground(AppTheme.TEXT);
        titles.add(t);
        titles.add(AppTheme.muted(subtitle));
        JPanel btns = new JPanel(new WrapLayout(FlowLayout.LEFT, 8, 4));
        btns.setOpaque(false);
        for (JButton b : actions) btns.add(b);
        JPanel bottomBar = new JPanel(new BorderLayout(0, 6));
        bottomBar.setOpaque(false);
        if (filters != null) bottomBar.add(filters, BorderLayout.NORTH);
        bottomBar.add(btns, BorderLayout.CENTER);
        top.add(titles, BorderLayout.NORTH);
        top.add(bottomBar, BorderLayout.SOUTH);

        card.add(top, BorderLayout.NORTH);
        card.add(AppTheme.wrapTable(table), BorderLayout.CENTER);
        p.add(card, BorderLayout.CENTER);
        return p;
    }

    private JPanel filterBar(String label1, Component field1, String label2, Component field2) {
        JPanel bar = new JPanel(new WrapLayout(FlowLayout.LEFT, 8, 0));
        bar.setOpaque(false);
        if (label1 != null && field1 != null) {
            JLabel l1 = new JLabel(label1 + ":");
            l1.setFont(AppTheme.SMALL);
            l1.setForeground(AppTheme.MUTED);
            bar.add(l1);
            bar.add(field1);
        }
        if (label2 != null && field2 != null) {
            JLabel l2 = new JLabel(label2 + ":");
            l2.setFont(AppTheme.SMALL);
            l2.setForeground(AppTheme.MUTED);
            bar.add(l2);
            bar.add(field2);
        }
        return bar;
    }

    private String searchText(JTextField field) {
        return field == null ? "" : field.getText().trim().toLowerCase(Locale.ROOT);
    }

    private String comboValue(JComboBox<String> combo) {
        Object v = combo == null ? null : combo.getSelectedItem();
        return v == null ? "Tất cả" : v.toString();
    }

    private boolean contains(String query, String... values) {
        for (String v : values) {
            if (v != null && v.toLowerCase(Locale.ROOT).contains(query)) return true;
        }
        return false;
    }

    private interface SimpleDocListener extends DocumentListener {
        void changed(DocumentEvent e);

        @Override
        default void insertUpdate(DocumentEvent e) { changed(e); }

        @Override
        default void removeUpdate(DocumentEvent e) { changed(e); }

        @Override
        default void changedUpdate(DocumentEvent e) { changed(e); }
    }

    private JPanel formPanel(Object... labelAndField) {
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1;
        int y = 0;
        for (int i = 0; i < labelAndField.length; i += 2) {
            JLabel l = new JLabel(String.valueOf(labelAndField[i]));
            l.setFont(AppTheme.SMALL);
            l.setForeground(AppTheme.MUTED);
            g.gridx = 0;
            g.gridy = y++;
            g.insets = new Insets(8, 0, 4, 0);
            form.add(l, g);
            g.gridy = y++;
            g.insets = new Insets(0, 0, 4, 0);
            form.add((Component) labelAndField[i + 1], g);
        }
        form.setPreferredSize(new Dimension(360, form.getPreferredSize().height));
        return form;
    }

    private DefaultTableModel model(String... cols) {
        return new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    private void applyStatusColumn(JTable table, int column) {
        if (table.getColumnCount() > column) {
            table.getColumnModel().getColumn(column).setCellRenderer(new StatusCellRenderer());
        }
    }

    private String initials(String name) {
        if (name == null || name.isBlank()) return "U";
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) return parts[0].substring(0, 1).toUpperCase();
        return (parts[0].charAt(0) + "" + parts[parts.length - 1].charAt(0)).toUpperCase();
    }

    private void error(Exception e) {
        JOptionPane.showMessageDialog(this, e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
    }
}
