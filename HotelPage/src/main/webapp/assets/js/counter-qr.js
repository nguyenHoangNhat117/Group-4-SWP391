(() => {
    const panel = document.getElementById("counterQr");
    if (!panel) return;
    const message = document.getElementById("counterQrState");
    const messages = {
        paid: "Đã ghi nhận thanh toán. Đang cập nhật số đã thu và còn thiếu…",
        closed: "QR đã đóng, không có tiền được ghi nhận. Về hồ sơ booking để kiểm tra.",
        rejected: "payOS từ chối tạo QR. Chưa ghi nhận thu tiền. Về hồ sơ booking để kiểm tra cấu hình.",
        review: "Có tiền chuyển hoặc dữ liệu cần đối soát. Chưa xác nhận thu thành công; không thu thêm.",
        pending: "Đang chờ thanh toán hoặc chờ ngân hàng xác nhận. Không thu thêm bằng tiền mặt.",
        unavailable: "Chưa kiểm tra được kết quả. Không coi là thất bại và không thu lại; hệ thống sẽ thử tiếp."
    };
    function display(state) {
        message.textContent = messages[state] || messages.unavailable;
        if (["paid", "closed", "review", "rejected"].includes(state)) {
            document.getElementById("counterQrFrame")?.remove();
            document.getElementById("counterQrLink")?.remove();
        }
        if (state === "paid") {
            window.location.replace(panel.dataset.backUrl);
        }
        return !["paid", "closed", "rejected"].includes(state);
    }
    async function poll() {
        let again = true;
        try {
            const response = await fetch(panel.dataset.statusUrl, {
                cache: "no-store", credentials: "same-origin", redirect: "error"
            });
            if (!response.ok) throw new Error("Status unavailable");
            again = display((await response.text()).trim());
        } catch (error) {
            display("unavailable");
        }
        if (again) window.setTimeout(poll, 6000);
    }
    if (display(panel.dataset.state)) poll();
})();
