//
//  ReminderWidget.swift
//  ReminderWidget
//
//  Created by G1-Huong.Pham-Dev on 23/10/2024.
//
//
//import SwiftUI
//import WidgetKit
//
//struct Provider: TimelineProvider {
//  func placeholder(in context: Context) -> ReminderEntry {
//    ReminderEntry(date: Date(), count: 0)
//  }
//
//  func getSnapshot(in context: Context, completion: @escaping (ReminderEntry) -> Void) {
//    // Get the UserDefaults for the AppGroup
//    let prefs = UserDefaults(suiteName: "group.com.example.homeWidgetReminder")
//    // Load the current Count
//    let entry = ReminderEntry(date: Date(), count: prefs?.integer(forKey: "counter") ?? 0)
//    completion(entry)
//  }
//
//  func getTimeline(in context: Context, completion: @escaping (Timeline<Entry>) -> Void) {
//    getSnapshot(in: context) { (entry) in
//      let timeline = Timeline(entries: [entry], policy: .atEnd)
//      completion(timeline)
//    }
//  }
//}
//
//struct ReminderEntry: TimelineEntry {
//  let date: Date
//  let count: Int
//}
//
//struct ReminderWidgetEntryView: View {
//  var entry: Provider.Entry
//
//  @Environment(\.widgetFamily) var family
//
//  var body: some View {
//    if family == .accessoryCircular {
//      Image(
//        uiImage: UIImage(
//          contentsOfFile: UserDefaults(suiteName: "group.com.example.homeWidgetReminder")?.string(
//            forKey: "dash_counter") ?? "")!
//      ).resizable()
//        .frame(width: 76, height: 76)
//        .scaledToFill()
//    } else {
//      VStack {
//        Text("You have pushed the button this many times:").font(.caption2).frame(
//          maxWidth: .infinity, alignment: .center)
//        Spacer()
//        Text(entry.count.description).font(.title).frame(maxWidth: .infinity, alignment: .center)
//        Spacer()
//        HStack {
//          // This button is for clearing
//          Button(intent: BackgroundIntent(method: "clear")) {
//            Image(systemName: "xmark").font(.system(size: 16)).foregroundColor(.red).frame(
//              width: 24, height: 24)
//          }.buttonStyle(.plain).frame(alignment: .leading)
//          Spacer()
//          // This button is for incrementing
//          Button(intent: BackgroundIntent(method: "increment")) {
//            Image(systemName: "plus").font(.system(size: 16)).foregroundColor(.white)
//
//          }.frame(width: 24, height: 24)
//            .background(.blue)
//            .cornerRadius(12).frame(alignment: .trailing)
//        }
//      }
//    }
//  }
//}
//
//struct ReminderWidget: Widget {
//  let kind: String = "ReminderWidget"
//
//  var body: some WidgetConfiguration {
//    StaticConfiguration(kind: kind, provider: Provider()) { entry in
//      if #available(iOS 17.0, *) {
//        ReminderWidgetEntryView(entry: entry)
//          .containerBackground(.fill.tertiary, for: .widget)
//      } else {
//        ReminderWidgetEntryView(entry: entry)
//          .padding()
//          .background()
//      }
//    }
//    .configurationDisplayName("Counter Widget")
//    .description("Count the Number Up")
//    .supportedFamilies([
//      .systemSmall, .systemMedium, .systemLarge, .systemExtraLarge, .accessoryCircular,
//    ])
//  }
//}
//
//#Preview(as: .systemSmall){
//  ReminderWidget()
//} timeline: {
//  ReminderEntry(date: .now, count: 0)
//}
//

import Foundation

struct RandomUserResponse: Codable {
    let results: [User]
}

struct User: Codable {
    let name: Name
    let gender: String
    let email: String
    let picture: Picture
}

struct Name: Codable {
    let first: String
    let last: String
}

struct Picture: Codable {
    let large: String
}

import SwiftUI
import WidgetKit

struct Provider: TimelineProvider {
    func placeholder(in context: Context) -> UserEntry {
        UserEntry(date: Date(), user: User(name: Name(first: "Loading", last: ""), gender: "female", email: "", picture: Picture(large: "")))
    }

    func getSnapshot(in context: Context, completion: @escaping (UserEntry) -> Void) {
        let entry = UserEntry(date: Date(), user: User(name: Name(first: "Loading", last: ""), gender: "female", email: "", picture: Picture(large: "")))
        completion(entry)
    }

    func getTimeline(in context: Context, completion: @escaping (Timeline<UserEntry>) -> Void) {
        fetchData { user in
            let entry = UserEntry(date: Date(), user: user)
            let timeline = Timeline(entries: [entry], policy: .atEnd)
            completion(timeline)
        }
    }

    private func fetchData(completion: @escaping (User) -> Void) {
        guard let url = URL(string: "https://randomuser.me/api/?gender=female") else { return }
        
        let task = URLSession.shared.dataTask(with: url) { data, response, error in
            if let data = data {
                do {
                    let decodedResponse = try JSONDecoder().decode(RandomUserResponse.self, from: data)
                    completion(decodedResponse.results[0])
                } catch {
                    print("Failed to decode JSON: \(error)")
                }
            }
        }
        task.resume()
    }
}

struct UserEntry: TimelineEntry {
    let date: Date
    let user: User
}

///
/// Tạo view cho widget
///
struct UserWidgetEntryView: View {
    var entry: Provider.Entry

    var body: some View {
        VStack {
            Text("\(entry.user.name.first) \(entry.user.name.last)")
                .font(.headline)
            Text("Gender: \(entry.user.gender)")
                .font(.subheadline)
            Text("Email: \(entry.user.email)")
                .font(.subheadline)
            Button(intent: BackgroundIntent(method: "increment")) {
           Image(systemName: "plus").font(.system(size: 16)).foregroundColor(.red)
         }.frame(width: 24, height: 24)
                .background(.blue)
                .cornerRadius(12)
                .frame(alignment: .trailing)
            // AsyncImage(url: URL(string: "https://4.img-dpreview.com/files/p/E~TS590x0~articles/3925134721/0266554465.jpeg")) { image in
            //     image.resizable()
            // } placeholder: {
            //     Image(systemName: "photo.fill")
            // }
            // .frame(width: 120, height: 120)
        }
        .padding()
    }
}

struct UserWidget: Widget {
    let kind: String = "UserWidget"

    var body: some WidgetConfiguration {
        StaticConfiguration(kind: kind, provider: Provider()) { entry in
            if #available(iOS 17.0, *) {
       UserWidgetEntryView(entry: entry)
         .containerBackground(.fill.tertiary, for: .widget)
     } else {
            UserWidgetEntryView(entry: entry)
     }

            
        }
        .configurationDisplayName("Random Female User")
        .description("Displays a random female user from the API.")
        .supportedFamilies([.systemSmall])
    }
}

struct UserWidgetLong: Widget {
    let kind: String = "UserWidgetLong"

    var body: some WidgetConfiguration {
        StaticConfiguration(kind: kind, provider: Provider()) { entry in
            if #available(iOS 17.0, *) {
       UserWidgetEntryView(entry: entry)
         .containerBackground(.fill.tertiary, for: .widget)
     } else {
            UserWidgetEntryView(entry: entry)
     }

            
        }
        .configurationDisplayName("Random Female User Long")
        .description("Displays a long random female user from the API.")
        .supportedFamilies([.systemMedium, .systemLarge])
    }
}


#Preview(as: .systemSmall){
    UserWidget()
} timeline: {
    UserEntry(date: .now, user: User(name: Name(first: "Loading", last: ""), gender: "female", email: "", picture: Picture(large: "")))
}

// https://forums.developer.apple.com/forums/thread/652581

