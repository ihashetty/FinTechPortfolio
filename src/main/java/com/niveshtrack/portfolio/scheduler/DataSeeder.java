package com.niveshtrack.portfolio.scheduler;

import com.niveshtrack.portfolio.entity.*;
import com.niveshtrack.portfolio.repository.*;
import com.niveshtrack.portfolio.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * Seeds stock and mutual-fund master data on application startup.
 * Demo-user data is only seeded in the {@code dev} Spring profile.
 *
 * <p>To prevent double-seeding, checks are performed before inserting.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final StockRepository stockRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final MutualFundRepository mutualFundRepository;
    private final AccountLedgerRepository accountLedgerRepository;
    private final PasswordEncoder passwordEncoder;
    private final WalletService walletService;
    private final Environment environment;

    @Override
    public void run(String... args) {
        seedStocks();
        seedMutualFunds();

        if (Arrays.asList(environment.getActiveProfiles()).contains("dev")) {
            seedDemoUser();
        } else {
            log.info("Skipping demo user seeding outside dev profile.");
        }
    }

    // ===== Stocks =====

    private void seedStocks() {
        if (stockRepository.count() > 0) {
            log.info("Stocks table already seeded. Skipping.");
            return;
        }

        List<Stock> stocks = createNSEStocks();
        stockRepository.saveAll(stocks);
        log.info("Seeded {} NSE stocks.", stocks.size());
    }

    private List<Stock> createNSEStocks() {
        return Arrays.asList(
            // ===== Nifty 50 =====
            stock("RELIANCE",   "Reliance Industries Ltd",              "Energy",               new BigDecimal("2850.00")),
            stock("TCS",        "Tata Consultancy Services Ltd",        "Information Technology",new BigDecimal("3950.00")),
            stock("HDFCBANK",   "HDFC Bank Ltd",                        "Banking",              new BigDecimal("1650.00")),
            stock("INFY",       "Infosys Ltd",                          "Information Technology",new BigDecimal("1780.00")),
            stock("ICICIBANK",  "ICICI Bank Ltd",                       "Banking",              new BigDecimal("1240.00")),
            stock("BHARTIARTL", "Bharti Airtel Ltd",                    "Telecom",              new BigDecimal("1580.00")),
            stock("SBIN",       "State Bank of India",                  "Banking",              new BigDecimal("820.00")),
            stock("KOTAKBANK",  "Kotak Mahindra Bank Ltd",              "Banking",              new BigDecimal("1790.00")),
            stock("LT",         "Larsen & Toubro Ltd",                  "Capital Goods",        new BigDecimal("3600.00")),
            stock("WIPRO",      "Wipro Ltd",                            "Information Technology",new BigDecimal("520.00")),
            stock("HCLTECH",    "HCL Technologies Ltd",                 "Information Technology",new BigDecimal("1650.00")),
            stock("BAJFINANCE", "Bajaj Finance Ltd",                    "NBFC",                 new BigDecimal("7200.00")),
            stock("ASIANPAINT", "Asian Paints Ltd",                     "Consumer Goods",       new BigDecimal("2800.00")),
            stock("MARUTI",     "Maruti Suzuki India Ltd",              "Automobile",           new BigDecimal("12500.00")),
            stock("SUNPHARMA",  "Sun Pharmaceutical Industries Ltd",    "Pharma",               new BigDecimal("1680.00")),
            stock("TITAN",      "Titan Company Ltd",                    "Consumer Goods",       new BigDecimal("3500.00")),
            stock("ULTRACEMCO", "UltraTech Cement Ltd",                 "Cement",               new BigDecimal("11000.00")),
            stock("NESTLEIND",  "Nestle India Ltd",                     "FMCG",                 new BigDecimal("2300.00")),
            stock("ITC",        "ITC Ltd",                              "FMCG",                 new BigDecimal("460.00")),
            stock("AXISBANK",   "Axis Bank Ltd",                        "Banking",              new BigDecimal("1180.00")),
            stock("M&M",        "Mahindra & Mahindra Ltd",              "Automobile",           new BigDecimal("2980.00")),
            stock("ONGC",       "Oil and Natural Gas Corporation Ltd",  "Energy",               new BigDecimal("265.00")),
            stock("POWERGRID",  "Power Grid Corporation of India Ltd",  "Utilities",            new BigDecimal("335.00")),
            stock("NTPC",       "NTPC Ltd",                             "Utilities",            new BigDecimal("380.00")),
            stock("TECHM",      "Tech Mahindra Ltd",                    "Information Technology",new BigDecimal("1580.00")),
            stock("BAJAJFINSV", "Bajaj Finserv Ltd",                    "NBFC",                 new BigDecimal("1650.00")),
            stock("GRASIM",     "Grasim Industries Ltd",                "Diversified",          new BigDecimal("2850.00")),
            stock("DIVISLAB",   "Divi's Laboratories Ltd",              "Pharma",               new BigDecimal("4800.00")),
            stock("DRREDDY",    "Dr Reddy's Laboratories Ltd",          "Pharma",               new BigDecimal("6900.00")),
            stock("CIPLA",      "Cipla Ltd",                            "Pharma",               new BigDecimal("1580.00")),
            stock("HINDUNILVR", "Hindustan Unilever Ltd",               "FMCG",                 new BigDecimal("2520.00")),
            stock("ADANIENT",   "Adani Enterprises Ltd",                "Diversified",          new BigDecimal("2400.00")),
            stock("ADANIPORTS", "Adani Ports and SEZ Ltd",              "Infrastructure",       new BigDecimal("1350.00")),
            stock("TATAMOTORS", "Tata Motors Ltd",                      "Automobile",           new BigDecimal("980.00")),
            stock("TATASTEEL",  "Tata Steel Ltd",                       "Metals",               new BigDecimal("165.00")),
            stock("JSWSTEEL",   "JSW Steel Ltd",                        "Metals",               new BigDecimal("920.00")),
            stock("COALINDIA",  "Coal India Ltd",                       "Mining",               new BigDecimal("480.00")),
            stock("BPCL",       "Bharat Petroleum Corporation Ltd",     "Energy",               new BigDecimal("620.00")),
            stock("EICHERMOT",  "Eicher Motors Ltd",                    "Automobile",           new BigDecimal("4600.00")),
            stock("APOLLOHOSP", "Apollo Hospitals Enterprise Ltd",      "Healthcare",           new BigDecimal("6800.00")),
            stock("HEROMOTOCO", "Hero MotoCorp Ltd",                    "Automobile",           new BigDecimal("5200.00")),
            stock("INDUSINDBK", "IndusInd Bank Ltd",                    "Banking",              new BigDecimal("1420.00")),
            stock("SBILIFE",    "SBI Life Insurance Company Ltd",       "Insurance",            new BigDecimal("1680.00")),
            stock("HDFCLIFE",   "HDFC Life Insurance Company Ltd",      "Insurance",            new BigDecimal("620.00")),
            stock("BRITANNIA",  "Britannia Industries Ltd",             "FMCG",                 new BigDecimal("5400.00")),
            stock("HINDALCO",   "Hindalco Industries Ltd",              "Metals",               new BigDecimal("680.00")),
            stock("TRENT",      "Trent Ltd",                            "Retail",               new BigDecimal("7200.00")),
            stock("SHRIRAMFIN", "Shriram Finance Ltd",                  "NBFC",                 new BigDecimal("2850.00")),
            stock("BEL",        "Bharat Electronics Ltd",               "Defence",              new BigDecimal("320.00")),

            // ===== Nifty Next 50 =====
            stock("BANKBARODA", "Bank of Baroda",                       "Banking",              new BigDecimal("265.00")),
            stock("PNB",        "Punjab National Bank",                 "Banking",              new BigDecimal("128.00")),
            stock("CANBK",      "Canara Bank",                          "Banking",              new BigDecimal("115.00")),
            stock("IDFCFIRSTB", "IDFC First Bank Ltd",                  "Banking",              new BigDecimal("72.00")),
            stock("FEDERALBNK", "Federal Bank Ltd",                     "Banking",              new BigDecimal("195.00")),
            stock("BANDHANBNK", "Bandhan Bank Ltd",                     "Banking",              new BigDecimal("210.00")),
            stock("GODREJCP",   "Godrej Consumer Products Ltd",         "FMCG",                 new BigDecimal("1350.00")),
            stock("DABUR",      "Dabur India Ltd",                      "FMCG",                 new BigDecimal("560.00")),
            stock("MARICO",     "Marico Ltd",                           "FMCG",                 new BigDecimal("640.00")),
            stock("COLPAL",     "Colgate-Palmolive India Ltd",          "FMCG",                 new BigDecimal("2600.00")),
            stock("PIDILITIND", "Pidilite Industries Ltd",              "Chemicals",            new BigDecimal("3200.00")),
            stock("HAVELLS",    "Havells India Ltd",                    "Consumer Durables",    new BigDecimal("1750.00")),
            stock("VOLTAS",     "Voltas Ltd",                           "Consumer Durables",    new BigDecimal("1850.00")),
            stock("SIEMENS",    "Siemens Ltd",                          "Capital Goods",        new BigDecimal("7500.00")),
            stock("ABB",        "ABB India Ltd",                        "Capital Goods",        new BigDecimal("8200.00")),
            stock("CUMMINSIND", "Cummins India Ltd",                    "Capital Goods",        new BigDecimal("3600.00")),
            stock("BHEL",       "Bharat Heavy Electricals Ltd",         "Capital Goods",        new BigDecimal("280.00")),
            stock("HAL",        "Hindustan Aeronautics Ltd",            "Defence",              new BigDecimal("5600.00")),
            stock("IRCTC",      "Indian Railway Catering & Tourism",    "Tourism",              new BigDecimal("850.00")),
            stock("TATAPOWER",  "Tata Power Company Ltd",               "Utilities",            new BigDecimal("435.00")),
            stock("ADANIGREEN", "Adani Green Energy Ltd",               "Renewable Energy",     new BigDecimal("1850.00")),
            stock("NHPC",       "NHPC Ltd",                             "Utilities",            new BigDecimal("95.00")),
            stock("RECLTD",     "REC Ltd",                              "NBFC",                 new BigDecimal("580.00")),
            stock("PFC",        "Power Finance Corporation Ltd",        "NBFC",                 new BigDecimal("520.00")),
            stock("CHOLAFIN",   "Cholamandalam Investment & Finance",   "NBFC",                 new BigDecimal("1450.00")),
            stock("MUTHOOTFIN", "Muthoot Finance Ltd",                  "NBFC",                 new BigDecimal("1850.00")),
            stock("ICICIPRULI",  "ICICI Prudential Life Insurance",     "Insurance",            new BigDecimal("680.00")),
            stock("ICICIGI",    "ICICI Lombard General Insurance",      "Insurance",            new BigDecimal("1780.00")),
            stock("NAUKRI",     "Info Edge (India) Ltd",                "Internet",             new BigDecimal("7200.00")),
            stock("ZOMATO",     "Zomato Ltd",                           "Internet",             new BigDecimal("265.00")),
            stock("PAYTM",      "One 97 Communications Ltd",            "Fintech",              new BigDecimal("980.00")),
            stock("DMART",      "Avenue Supermarts Ltd",                "Retail",               new BigDecimal("3800.00")),
            stock("TATACONSUM", "Tata Consumer Products Ltd",           "FMCG",                 new BigDecimal("1120.00")),
            stock("BERGEPAINT", "Berger Paints India Ltd",              "Consumer Goods",       new BigDecimal("580.00")),
            stock("PAGEIND",    "Page Industries Ltd",                  "Textiles",             new BigDecimal("42000.00")),
            stock("MPHASIS",    "Mphasis Ltd",                          "Information Technology",new BigDecimal("3100.00")),
            stock("LTIM",       "LTIMindtree Ltd",                      "Information Technology",new BigDecimal("5800.00")),
            stock("PERSISTENT", "Persistent Systems Ltd",               "Information Technology",new BigDecimal("6200.00")),
            stock("COFORGE",    "Coforge Ltd",                          "Information Technology",new BigDecimal("7800.00")),

            // ===== Popular Mid & Small Caps =====
            stock("TATAELXSI",  "Tata Elxsi Ltd",                      "Information Technology",new BigDecimal("6500.00")),
            stock("DIXON",      "Dixon Technologies India Ltd",          "Electronics",          new BigDecimal("14500.00")),
            stock("POLYCAB",    "Polycab India Ltd",                    "Consumer Durables",    new BigDecimal("6800.00")),
            stock("AUROPHARMA", "Aurobindo Pharma Ltd",                 "Pharma",               new BigDecimal("1280.00")),
            stock("LUPIN",      "Lupin Ltd",                            "Pharma",               new BigDecimal("2100.00")),
            stock("BIOCON",     "Biocon Ltd",                           "Pharma",               new BigDecimal("340.00")),
            stock("TORNTPHARM", "Torrent Pharmaceuticals Ltd",          "Pharma",               new BigDecimal("3400.00")),
            stock("IPCALAB",    "IPCA Laboratories Ltd",                "Pharma",               new BigDecimal("1520.00")),
            stock("ALKEM",      "Alkem Laboratories Ltd",               "Pharma",               new BigDecimal("5600.00")),
            stock("LAURUSLABS", "Laurus Labs Ltd",                      "Pharma",               new BigDecimal("560.00")),
            stock("SYNGENE",    "Syngene International Ltd",            "Pharma",               new BigDecimal("820.00")),
            stock("IOC",        "Indian Oil Corporation Ltd",           "Energy",               new BigDecimal("168.00")),
            stock("HINDPETRO",  "Hindustan Petroleum Corp Ltd",         "Energy",               new BigDecimal("380.00")),
            stock("GAIL",       "GAIL (India) Ltd",                     "Energy",               new BigDecimal("210.00")),
            stock("PETRONET",   "Petronet LNG Ltd",                     "Energy",               new BigDecimal("340.00")),
            stock("VEDL",       "Vedanta Ltd",                          "Metals",               new BigDecimal("480.00")),
            stock("NMDC",       "NMDC Ltd",                             "Mining",               new BigDecimal("260.00")),
            stock("SAIL",       "Steel Authority of India Ltd",         "Metals",               new BigDecimal("145.00")),
            stock("NATIONALUM", "National Aluminium Co Ltd",            "Metals",               new BigDecimal("185.00")),
            stock("JINDALSTEL", "Jindal Steel & Power Ltd",             "Metals",               new BigDecimal("980.00")),
            stock("TVSMOTOR",   "TVS Motor Company Ltd",                "Automobile",           new BigDecimal("2600.00")),
            stock("BAJAJ-AUTO", "Bajaj Auto Ltd",                       "Automobile",           new BigDecimal("9500.00")),
            stock("ASHOKLEY",   "Ashok Leyland Ltd",                    "Automobile",           new BigDecimal("215.00")),
            stock("MOTHERSON",  "Motherson Sumi Wiring India Ltd",     "Auto Ancillary",       new BigDecimal("165.00")),
            stock("BALKRISIND", "Balkrishna Industries Ltd",            "Auto Ancillary",       new BigDecimal("3100.00")),
            stock("MRF",        "MRF Ltd",                              "Auto Ancillary",       new BigDecimal("135000.00")),
            stock("BOSCHLTD",   "Bosch Ltd",                            "Auto Ancillary",       new BigDecimal("32000.00")),
            stock("AMBUJACEM",  "Ambuja Cements Ltd",                   "Cement",               new BigDecimal("640.00")),
            stock("SHREECEM",   "Shree Cement Ltd",                     "Cement",               new BigDecimal("26000.00")),
            stock("ACC",        "ACC Ltd",                              "Cement",               new BigDecimal("2450.00")),
            stock("RAMCOCEM",   "Ramco Cements Ltd",                    "Cement",               new BigDecimal("980.00")),
            stock("DLF",        "DLF Ltd",                              "Real Estate",          new BigDecimal("880.00")),
            stock("GODREJPROP", "Godrej Properties Ltd",                "Real Estate",          new BigDecimal("2800.00")),
            stock("OBEROIRLTY", "Oberoi Realty Ltd",                    "Real Estate",          new BigDecimal("1850.00")),
            stock("PRESTIGE",   "Prestige Estates Projects Ltd",        "Real Estate",          new BigDecimal("1680.00")),
            stock("LODHA",      "Macrotech Developers Ltd",             "Real Estate",          new BigDecimal("1450.00")),
            stock("LICI",       "Life Insurance Corp of India",         "Insurance",            new BigDecimal("1020.00")),
            stock("NIACL",      "New India Assurance Co Ltd",           "Insurance",            new BigDecimal("280.00")),
            stock("MFSL",       "Max Financial Services Ltd",           "Insurance",            new BigDecimal("1150.00")),
            stock("SBICARD",    "SBI Cards & Payment Services Ltd",     "Fintech",              new BigDecimal("780.00")),
            stock("LICHSGFIN",  "LIC Housing Finance Ltd",              "NBFC",                 new BigDecimal("520.00")),
            stock("MANAPPURAM", "Manappuram Finance Ltd",               "NBFC",                 new BigDecimal("210.00")),
            stock("L&TFH",      "L&T Finance Ltd",                      "NBFC",                 new BigDecimal("175.00")),
            stock("JUBLFOOD",   "Jubilant FoodWorks Ltd",               "Food & Beverage",      new BigDecimal("580.00")),
            stock("UBL",        "United Breweries Ltd",                 "Food & Beverage",      new BigDecimal("2100.00")),
            stock("VBL",        "Varun Beverages Ltd",                  "Food & Beverage",      new BigDecimal("1650.00")),
            stock("TATACOMM",   "Tata Communications Ltd",              "Telecom",              new BigDecimal("1850.00")),
            stock("IDEA",       "Vodafone Idea Ltd",                    "Telecom",              new BigDecimal("14.00")),
            stock("INDUSTOWER", "Indus Towers Ltd",                     "Telecom",              new BigDecimal("380.00")),
            stock("DEEPAKNTR",  "Deepak Nitrite Ltd",                   "Chemicals",            new BigDecimal("2800.00")),
            stock("ATUL",       "Atul Ltd",                             "Chemicals",            new BigDecimal("7200.00")),
            stock("SRF",        "SRF Ltd",                              "Chemicals",            new BigDecimal("2800.00")),
            stock("PIIND",      "PI Industries Ltd",                    "Chemicals",            new BigDecimal("4200.00")),
            stock("UPL",        "UPL Ltd",                              "Chemicals",            new BigDecimal("580.00")),
            stock("ASTRAL",     "Astral Ltd",                           "Building Materials",   new BigDecimal("2200.00")),
            stock("SUPREMEIND", "Supreme Industries Ltd",               "Building Materials",   new BigDecimal("5400.00")),
            stock("CROMPTON",   "Crompton Greaves Consumer Electricals","Consumer Durables",    new BigDecimal("380.00")),
            stock("WHIRLPOOL",  "Whirlpool of India Ltd",               "Consumer Durables",    new BigDecimal("1450.00")),
            stock("BATAINDIA",  "Bata India Ltd",                       "Consumer Goods",       new BigDecimal("1650.00")),
            stock("RELAXO",     "Relaxo Footwears Ltd",                 "Consumer Goods",       new BigDecimal("860.00")),
            stock("INDHOTEL",   "Indian Hotels Company Ltd",            "Hospitality",          new BigDecimal("780.00")),
            stock("LEMON",      "Lemon Tree Hotels Ltd",                "Hospitality",          new BigDecimal("145.00")),
            stock("LTTS",       "L&T Technology Services Ltd",          "Information Technology",new BigDecimal("5600.00")),
            stock("HAPPSTMNDS", "Happiest Minds Technologies Ltd",      "Information Technology",new BigDecimal("820.00")),
            stock("ZYDUSLIFE",  "Zydus Lifesciences Ltd",               "Pharma",               new BigDecimal("1050.00")),
            stock("MAXHEALTH",  "Max Healthcare Institute Ltd",         "Healthcare",           new BigDecimal("950.00")),
            stock("FORTIS",     "Fortis Healthcare Ltd",                "Healthcare",           new BigDecimal("520.00")),
            stock("METROPOLIS", "Metropolis Healthcare Ltd",            "Healthcare",           new BigDecimal("1850.00")),
            stock("DELHIVERY",  "Delhivery Ltd",                        "Logistics",            new BigDecimal("420.00")),
            stock("CONCOR",     "Container Corp of India Ltd",          "Logistics",            new BigDecimal("780.00")),
            stock("IRFC",       "Indian Railway Finance Corp Ltd",      "NBFC",                 new BigDecimal("168.00")),
            stock("INDIANB",    "Indian Bank",                          "Banking",              new BigDecimal("580.00")),
            stock("AUBANK",     "AU Small Finance Bank Ltd",            "Banking",              new BigDecimal("680.00")),
            stock("CUB",        "City Union Bank Ltd",                  "Banking",              new BigDecimal("160.00")),
            stock("KARURVYSYA", "Karur Vysya Bank Ltd",                 "Banking",              new BigDecimal("210.00")),
            stock("IDBI",       "IDBI Bank Ltd",                        "Banking",              new BigDecimal("98.00")),
            stock("JIOFIN",     "Jio Financial Services Ltd",           "NBFC",                 new BigDecimal("340.00")),
            stock("HUDCO",      "Housing & Urban Development Corp",     "NBFC",                 new BigDecimal("240.00")),
            stock("OFSS",       "Oracle Financial Services Software",   "Information Technology",new BigDecimal("11500.00")),
            stock("RATNAMANI",  "Ratnamani Metals & Tubes Ltd",         "Metals",               new BigDecimal("3400.00")),
            stock("NAVINFLUOR", "Navin Fluorine International Ltd",     "Chemicals",            new BigDecimal("3800.00")),
            stock("CLEAN",      "Clean Science & Technology Ltd",       "Chemicals",            new BigDecimal("1450.00")),
            stock("SONACOMS",   "Sona BLW Precision Forgings Ltd",     "Auto Ancillary",       new BigDecimal("680.00")),
            stock("KAYNES",     "Kaynes Technology India Ltd",          "Electronics",          new BigDecimal("5200.00")),
            stock("TIINDIA",    "Tube Investments of India Ltd",        "Capital Goods",        new BigDecimal("4200.00")),
            stock("CESC",       "CESC Ltd",                             "Utilities",            new BigDecimal("185.00")),
            stock("TORNTPOWER", "Torrent Power Ltd",                    "Utilities",            new BigDecimal("1850.00")),
            stock("JSWENERGY",  "JSW Energy Ltd",                       "Utilities",            new BigDecimal("680.00")),
            stock("SJVN",       "SJVN Ltd",                             "Utilities",            new BigDecimal("130.00")),
            stock("IEX",        "Indian Energy Exchange Ltd",           "Utilities",            new BigDecimal("165.00")),
            stock("CDSL",       "Central Depository Services Ltd",      "Fintech",              new BigDecimal("2400.00")),
            stock("BSE",        "BSE Ltd",                              "Fintech",              new BigDecimal("3200.00")),
            stock("MCX",        "Multi Commodity Exchange of India",    "Fintech",              new BigDecimal("6200.00")),
            stock("CAMS",       "Computer Age Management Services",     "Fintech",              new BigDecimal("3800.00")),
            stock("ANGELONE",   "Angel One Ltd",                        "Fintech",              new BigDecimal("3200.00"))
        );
    }

    private Stock stock(String symbol, String name, String sector, BigDecimal price) {
        return Stock.builder()
                .symbol(symbol)
                .name(name)
                .sector(sector)
                .currentPrice(price)
                .lastUpdated(LocalDateTime.now())
                .build();
    }

    // ===== Demo User =====

    private void seedDemoUser() {
        if (userRepository.existsByEmail("arjun@example.com")) {
            log.info("Demo user already exists. Skipping.");
            return;
        }

        User user = User.builder()
                .name("Arjun Sharma")
                .email("arjun@example.com")
                .passwordHash(passwordEncoder.encode("password123"))
                .currency("INR")
                .darkMode(false)
                .build();

        userRepository.save(user);
        log.info("Demo user created: arjun@example.com / password123");

        // Seed initial wallet balance ₹10,00,000
        walletService.deposit(user.getId(), new BigDecimal("1000000.00"));
        log.info("Seeded ₹10,00,000 wallet balance for demo user.");

        List<Transaction> transactions = createSampleTransactions(user);
        transactionRepository.saveAll(transactions);
        log.info("Seeded {} sample transactions for demo user.", transactions.size());
    }

    private List<Transaction> createSampleTransactions(User user) {
        return Arrays.asList(
            txn(user, "RELIANCE",   "Reliance Industries Ltd",          TransactionType.BUY,  50,  new BigDecimal("2400.00"), LocalDate.of(2023, 1, 10)),
            txn(user, "TCS",        "Tata Consultancy Services Ltd",     TransactionType.BUY,  20,  new BigDecimal("3200.00"), LocalDate.of(2023, 2, 15)),
            txn(user, "HDFCBANK",   "HDFC Bank Ltd",                     TransactionType.BUY,  100, new BigDecimal("1550.00"), LocalDate.of(2023, 3, 20)),
            txn(user, "INFY",       "Infosys Ltd",                       TransactionType.BUY,  60,  new BigDecimal("1450.00"), LocalDate.of(2023, 4, 5)),
            txn(user, "ICICIBANK",  "ICICI Bank Ltd",                    TransactionType.BUY,  80,  new BigDecimal("980.00"),  LocalDate.of(2023, 5, 12)),
            txn(user, "WIPRO",      "Wipro Ltd",                         TransactionType.BUY,  150, new BigDecimal("440.00"),  LocalDate.of(2023, 6, 1)),
            txn(user, "TCS",        "Tata Consultancy Services Ltd",     TransactionType.BUY,  10,  new BigDecimal("3400.00"), LocalDate.of(2023, 7, 18)),
            txn(user, "ITC",        "ITC Ltd",                           TransactionType.BUY,  500, new BigDecimal("420.00"),  LocalDate.of(2023, 8, 22)),
            txn(user, "RELIANCE",   "Reliance Industries Ltd",           TransactionType.SELL, 10,  new BigDecimal("2700.00"), LocalDate.of(2023, 9, 14)),
            txn(user, "HCLTECH",    "HCL Technologies Ltd",              TransactionType.BUY,  40,  new BigDecimal("1300.00"), LocalDate.of(2023, 10, 3)),
            txn(user, "SUNPHARMA",  "Sun Pharmaceutical Industries Ltd", TransactionType.BUY,  30,  new BigDecimal("1400.00"), LocalDate.of(2023, 11, 8)),
            txn(user, "BAJFINANCE", "Bajaj Finance Ltd",                 TransactionType.BUY,  10,  new BigDecimal("6800.00"), LocalDate.of(2023, 12, 1)),
            txn(user, "INFY",       "Infosys Ltd",                       TransactionType.SELL, 20,  new BigDecimal("1600.00"), LocalDate.of(2024, 1, 20)),
            txn(user, "TITAN",      "Titan Company Ltd",                 TransactionType.BUY,  25,  new BigDecimal("3200.00"), LocalDate.of(2024, 2, 14)),
            txn(user, "AXISBANK",   "Axis Bank Ltd",                     TransactionType.BUY,  70,  new BigDecimal("1050.00"), LocalDate.of(2024, 3, 5)),
            txn(user, "MARUTI",     "Maruti Suzuki India Ltd",           TransactionType.BUY,  5,   new BigDecimal("11500.00"),LocalDate.of(2024, 4, 10)),
            txn(user, "WIPRO",      "Wipro Ltd",                         TransactionType.SELL, 50,  new BigDecimal("520.00"),  LocalDate.of(2024, 5, 15)),
            txn(user, "NESTLEIND",  "Nestle India Ltd",                  TransactionType.BUY,  20,  new BigDecimal("2200.00"), LocalDate.of(2024, 6, 20))
        );
    }

    private Transaction txn(User user, String symbol, String name,
                             TransactionType type, int qty, BigDecimal price, LocalDate date) {
        return Transaction.builder()
                .user(user)
                .stockSymbol(symbol)
                .stockName(name)
                .type(type)
                .quantity(new BigDecimal(qty))
                .price(price)
                .transactionDate(date)
                .assetType(AssetType.STOCK)
                .brokerage(new BigDecimal("20.00"))
                .notes("Sample transaction (seeded)")
                .build();
    }

    // ===== Mutual Funds =====

    private void seedMutualFunds() {
        if (mutualFundRepository.count() > 0) {
            log.info("Mutual funds table already seeded. Skipping.");
            return;
        }

        List<MutualFund> funds = createMutualFunds();
        mutualFundRepository.saveAll(funds);
        log.info("Seeded {} mutual funds.", funds.size());
    }

    private List<MutualFund> createMutualFunds() {
        return Arrays.asList(
            mf("AXISBLUECHIP",   "Axis Bluechip Fund",                   "Large Cap",   new BigDecimal("48.50")),
            mf("MIRAELARCE",     "Mirae Asset Large Cap Fund",           "Large Cap",   new BigDecimal("92.30")),
            mf("PARAGNFLEXI",    "Parag Parikh Flexi Cap Fund",          "Flexi Cap",   new BigDecimal("68.75")),
            mf("SBISMALLCAP",    "SBI Small Cap Fund",                   "Small Cap",   new BigDecimal("142.60")),
            mf("HDFCMIDCAP",     "HDFC Mid-Cap Opportunities Fund",     "Mid Cap",     new BigDecimal("112.40")),
            mf("ABORSLNIFTY50",  "Aditya Birla SL Nifty 50 Index Fund", "Index",       new BigDecimal("198.25")),
            mf("ICICIPRUVALUE",  "ICICI Prudential Value Discovery Fund","Value",       new BigDecimal("356.80")),
            mf("KOTAKNIFTY",     "Kotak Nifty 50 Index Fund",           "Index",       new BigDecimal("215.70")),
            mf("NIPPONLIQUID",   "Nippon India Liquid Fund",            "Liquid",      new BigDecimal("5420.15")),
            mf("TATDIGITALIND",  "Tata Digital India Fund",             "Sectoral",    new BigDecimal("42.90"))
        );
    }

    private MutualFund mf(String symbol, String name, String category, BigDecimal nav) {
        return MutualFund.builder()
                .symbol(symbol)
                .name(name)
                .category(category)
                .nav(nav)
                .lastUpdated(LocalDateTime.now())
                .build();
    }
}
