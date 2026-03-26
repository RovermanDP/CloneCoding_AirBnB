export type InquiryStatus = "Awaiting reply" | "Replied";

export type InquiryThread = {
  id: number;
  guest: string;
  title: string;
  stay: string;
  room: string;
  status: InquiryStatus;
  lastReply: string | null;
  updatedAt: string;
};

export type InboxListResponse = {
  threads: InquiryThread[];
};

export type SendReplyPayload = {
  message: string;
};

export type SendReplyResponse = {
  thread: InquiryThread;
};
