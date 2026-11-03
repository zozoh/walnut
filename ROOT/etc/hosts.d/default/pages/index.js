// Keep mode consistent with the server's app-module-sys-login policy.
// domain scopes Captcha/OTP only; this page still submits a system login.
// initialize the window.SIGN_IN_CONFIG at index.html

$(function () {
  const $body = $(document.body);
  _.delay(() => $body.addClass("load-done"), 0);
  $body.css("background-image", `url('img/bg${_.random(0, 10)}.jpg')`);

  const form = document.querySelector(".login-form");
  const accountInput = form.elements.namedItem("name");
  const passwordInput = form.elements.namedItem("passwd");
  const otpInput = form.elements.namedItem("otp_val");
  const otpConfigInput = form.elements.namedItem("otp");
  const authWayBox = form.querySelector(".auth-way");
  const passwordBox = form.querySelector(".password-field");
  const otpBox = form.querySelector(".otp-field");
  const getOtpButton = form.querySelector(".get-otp");
  const errorBox = form.querySelector(".login-error");
  const dialog = document.querySelector(".captcha-dialog");
  const captchaBox = dialog.querySelector(".captcha-box");
  const domain = SIGN_IN_CONFIG.domain?.trim();
  let authWay = "password";
  let tac;
  let isSending = false;
  let cooldown = 0;
  let cooldownUntil = 0;
  let cooldownTimer;

  function showError(message, error) {
    errorBox.textContent = message;
    errorBox.hidden = !message;
    if (error) {
      console.error(message, error);
    }
  }

  function apiPath(name) {
    return `/a/${name}${domain ? `/${encodeURIComponent(domain)}` : ""}`;
  }

  function updateOtpButton() {
    getOtpButton.disabled =
      !accountInput.value.trim() ||
      (SIGN_IN_CONFIG.mode === "PASSWD_AND_OTP" && !passwordInput.value) ||
      isSending ||
      dialog.open ||
      cooldown > 0;
    getOtpButton.textContent =
      cooldown > 0 ? `${cooldown}s` : isSending ? "Sending..." : "Get OTP";
  }

  function closeCaptcha() {
    const current = tac;
    tac = undefined;
    current?.destroyWindow();
    captchaBox.replaceChildren();
    if (dialog.open) {
      dialog.close();
    }
    updateOtpButton();
  }

  function renderMode() {
    const mode = SIGN_IN_CONFIG.mode;
    const showPassword =
      mode === "PASSWD_ONLY" ||
      mode === "PASSWD_AND_OTP" ||
      (mode === "PASSWD_OR_OTP" && authWay === "password");
    const showOtp =
      mode === "OTP_ONLY" ||
      mode === "PASSWD_AND_OTP" ||
      (mode === "PASSWD_OR_OTP" && authWay === "otp");
    authWayBox.hidden = mode !== "PASSWD_OR_OTP";
    passwordBox.hidden = !showPassword;
    passwordInput.disabled = !showPassword;
    passwordInput.required = showPassword;
    otpBox.hidden = !showOtp;
    otpInput.disabled = !showOtp;
    otpInput.required = showOtp;
    otpConfigInput.disabled = !showOtp || !SIGN_IN_CONFIG.otp;
    otpConfigInput.value = SIGN_IN_CONFIG.otp || "";
    for (const button of authWayBox.querySelectorAll("button")) {
      button.setAttribute(
        "aria-pressed",
        String(button.dataset.authWay === authWay),
      );
    }
    updateOtpButton();
  }

  function resumeCooldown() {
    window.clearInterval(cooldownTimer);
    const tick = () => {
      cooldown = Math.max(0, Math.ceil((cooldownUntil - Date.now()) / 1000));
      if (cooldown <= 0) {
        window.clearInterval(cooldownTimer);
      }
      updateOtpButton();
    };
    tick();
    if (cooldown > 0) {
      cooldownTimer = window.setInterval(tick, 1000);
    }
  }

  function startCooldown() {
    cooldownUntil = Date.now() + Math.floor(SIGN_IN_CONFIG.otpCooldown) * 1000;
    resumeCooldown();
  }

  async function sendOtp(ticket, account) {
    if (isSending || cooldown > 0) {
      return;
    }
    isSending = true;
    updateOtpButton();
    const data = new URLSearchParams({ account, ticket });
    if (SIGN_IN_CONFIG.otp) {
      data.set("otp", SIGN_IN_CONFIG.otp);
    }
    if (SIGN_IN_CONFIG.captcha) {
      data.set("captcha", SIGN_IN_CONFIG.captcha);
    }
    try {
      const response = await fetch(apiPath("otp/send"), {
        method: "POST",
        body: data,
        signal: AbortSignal.timeout(15000),
      });
      if (!response.ok) {
        throw new Error(`HTTP ${response.status}`);
      }
      const result = await response.json();
      if (!result || typeof result !== "object" || Array.isArray(result)) {
        throw new Error("Invalid OTP response");
      }
      if (!result?.ok) {
        throw new Error(result?.errCode || result?.msg || "OTP sending failed");
      }
      startCooldown();
    } catch (error) {
      showError(`Unable to send OTP: ${error.message}`, error);
    } finally {
      isSending = false;
      updateOtpButton();
    }
  }

  // TAC ignores request-chain return values, so adapt the response in place.
  function adaptCaptchaResponse(response) {
    if (response.code === undefined && typeof response.ok === "boolean") {
      response.code = response.ok ? 200 : 4001;
    }
    if (response.code === undefined && typeof response.type === "string") {
      const captcha = { ...response };
      for (const key of Object.keys(response)) {
        delete response[key];
      }
      response.code = 200;
      response.data = captcha;
    }
    const captcha = response.data;
    if (
      response.code === 200 &&
      captcha?.type &&
      captcha.data &&
      typeof captcha.data === "object"
    ) {
      response.data = { ...captcha, ...captcha.data };
    }
  }

  function openCaptcha() {
    if (getOtpButton.disabled) {
      return;
    }
    showError("");
    const account = accountInput.value.trim();
    try {
      if (!window.TAC) {
        throw new Error("TAC script is unavailable");
      }
      const query = new URLSearchParams({ account });
      if (SIGN_IN_CONFIG.captcha) {
        query.set("captcha", SIGN_IN_CONFIG.captcha);
      }
      dialog.showModal();
      updateOtpButton();
      const instance = new window.TAC(
        {
          bindEl: captchaBox,
          requestCaptchaDataUrl: `${apiPath("captcha/get")}?${query}`,
          validCaptchaUrl: apiPath("captcha/verify"),
          validSuccess(response) {
            if (tac !== instance) {
              return;
            }
            const ticket =
              response.data?.ticket ||
              response.data?.id ||
              response.ticket ||
              response.id;
            closeCaptcha();
            if (!ticket) {
              showError("Captcha verification did not return an OTP ticket.");
              return;
            }
            void sendOtp(ticket, account);
          },
          btnCloseFun: closeCaptcha,
        },
        { logoUrl: null },
      );
      tac = instance;
      // The SDK swallows transport errors; surface them and bound request time.
      instance.config.doSendRequest = async (request) => {
        try {
          const response = await fetch(request.url, {
            method: request.method,
            headers: request.headers,
            body: JSON.stringify(request.data),
            signal: AbortSignal.timeout(15000),
          });
          if (!response.ok) {
            throw new Error(`HTTP ${response.status}`);
          }
          const result = await response.json();
          if (!result || typeof result !== "object" || Array.isArray(result)) {
            throw new Error("Invalid Captcha response");
          }
          if (tac !== instance) {
            throw new Error("Captcha was closed");
          }
          return result;
        } catch (error) {
          if (tac === instance) {
            closeCaptcha();
            showError(
              `Unable to load or verify Captcha: ${error.message}`,
              error,
            );
          }
          throw error;
        }
      };
      instance.config.insertRequestChain(0, {
        preRequest(type, request) {
          if (type === "validCaptcha") {
            request.data.domain = domain || null;
            request.data.account = account;
            request.data.captcha = SIGN_IN_CONFIG.captcha || null;
          }
          return true;
        },
        postRequest(type, request, response) {
          adaptCaptchaResponse(response);
          if (
            response.code !== 200 ||
            (type === "requestCaptchaData" && !response.data?.type)
          ) {
            const message =
              response.errCode || response.msg || "Invalid Captcha response";
            showError(message);
            if (type === "requestCaptchaData") {
              closeCaptcha();
              throw new Error(message);
            }
          }
          return true;
        },
      });
      instance.init();
    } catch (error) {
      closeCaptcha();
      showError(`Unable to open Captcha: ${error.message}`, error);
    }
  }

  for (const button of authWayBox.querySelectorAll("button")) {
    button.addEventListener("click", () => {
      closeCaptcha();
      authWay = button.dataset.authWay;
      showError("");
      renderMode();
    });
  }
  accountInput.addEventListener("input", updateOtpButton);
  passwordInput.addEventListener("input", updateOtpButton);
  getOtpButton.addEventListener("click", openCaptcha);
  dialog
    .querySelector(".captcha-close")
    .addEventListener("click", closeCaptcha);
  dialog.addEventListener("cancel", (event) => {
    event.preventDefault();
    closeCaptcha();
  });
  form.addEventListener("submit", (event) => {
    accountInput.value = accountInput.value.trim();
    otpInput.value = otpInput.value.trim();
    if (!form.reportValidity()) {
      event.preventDefault();
    }
  });
  window.addEventListener("pagehide", () => {
    closeCaptcha();
    window.clearInterval(cooldownTimer);
  });
  window.addEventListener("pageshow", resumeCooldown);

  if (
    !["PASSWD_AND_OTP", "PASSWD_OR_OTP", "OTP_ONLY", "PASSWD_ONLY"].includes(
      SIGN_IN_CONFIG.mode,
    ) ||
    !Number.isFinite(SIGN_IN_CONFIG.otpCooldown) ||
    SIGN_IN_CONFIG.otpCooldown < 0
  ) {
    showError("Invalid sign-in mode or OTP cooldown configuration.");
    for (const button of form.querySelectorAll("button")) {
      button.disabled = true;
    }
    form.addEventListener("submit", (event) => event.preventDefault());
    return;
  }
  renderMode();
});
